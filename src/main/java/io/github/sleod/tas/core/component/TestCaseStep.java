package io.github.sleod.tas.core.component;

import com.beust.jcommander.Strings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import io.github.sleod.tas.common.enumerations.ScreenshotLevel;
import io.github.sleod.tas.common.enumerations.TestStatus;
import io.github.sleod.tas.common.enumerations.TestType;
import io.github.sleod.tas.common.logging.ScreenCapture;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.core.annotations.*;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;
import io.github.sleod.tas.core.json.container.JSONTestCaseStep;
import io.github.sleod.tas.core.json.container.JsonTestCaseMetaData;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import io.github.sleod.tas.exception.TestDataEmptyException;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

import static io.github.sleod.tas.common.logging.SystemLogger.logStepInfo;
import static io.github.sleod.tas.common.logging.SystemLogger.warn;
import static io.github.sleod.tas.common.utils.StringTextUtils.isValid;

public class TestCaseStep implements Executable {

    public static final int PARAMETER_ROW_NUM = 0;
    @Getter
    private final int orderNumber;
    private final Class<?> testObjectClass;
    private final TestDataContainer testDataContainer;
    private final JsonTestCaseMetaData jsonTestCaseMetaData;
    private Object pageObject = null;
    private final JSONTestCaseStep jsonTestCaseStep;
    @Getter
    private final TestStepResult testStepResult;
    @Setter
    private boolean takeScreenshot = false;
    @Setter
    @Getter
    private boolean stopOnError = false;
    @Getter
    @Setter
    private boolean skipOnError = false;
    private final ObjectMapper mapper = ObjectMapperSingleton.mapper();
    @Getter
    private final String name;
    private TestRunResult testRunResult;
    private final Method runMethod;
    private boolean noRun = false;
    @Getter
    @Setter
    private Map<String, Object> parameters = new LinkedHashMap<>();

    /**
     * constructor
     *
     * @param orderNumber       order number of step in test case
     * @param jsonTestCaseMetaData metadata of test case
     * @param jStep             json test case step
     * @param testObjectClass   class of test object
     * @param testDataContainer container of test data
     */
    public TestCaseStep(int orderNumber, JsonTestCaseMetaData jsonTestCaseMetaData, JSONTestCaseStep jStep, Class<?> testObjectClass, TestDataContainer testDataContainer) {
        this.name = ("Step " + orderNumber + ". " + testObjectClass.getDeclaredAnnotation(TestObject.class)
                .name() + "@" + jStep.getName());
        this.jsonTestCaseStep = jStep;
        this.testDataContainer = testDataContainer;
        this.orderNumber = orderNumber;
        this.testObjectClass = testObjectClass;
        this.testStepResult = new TestStepResult(getName(), getOrderNumber());
        //get method with annotated name
        this.runMethod = getMethodWithAnnoName(jsonTestCaseStep.getName());
        this.jsonTestCaseMetaData = jsonTestCaseMetaData;
    }

    /**
     * prepare step for display in report
     *
     * @param retry      is retry process
     * @param retryOrder current retry order
     * @return name of step
     */
    public String prepareAndGetDisplayName(boolean retry, int retryOrder) {
        noRun = PropertyResolver.isRetryOnErrorEnabled() && retry
                && retryOrder >= 0 && retryOrder >= getOrderNumber() + PropertyResolver.getRetryOverSteps();
        return getName();
    }

    /**
     * before step actions
     */
    public void beforeStep() throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        testStepResult.startNow();
        if (runMethod == null) {
            throw new ExceptionBase(ExceptionErrorKeys.METHOD_NOT_FOUND, TestStep.class.getName(), jsonTestCaseStep.getName());
        }
        if (!Modifier.isPublic(runMethod.getModifiers())) {
            throw new ExceptionBase(ExceptionErrorKeys.METHOD_SHOULD_BE_PUBLIC, getName());
        }
        if (testDataContainer.getPageObject(testObjectClass.getName()) == null) {
            pageObject = testObjectClass.getDeclaredConstructor().newInstance();
            testDataContainer.addPageObject(testObjectClass.getName(), pageObject);
        } else {
            pageObject = testDataContainer.getPageObject(testObjectClass.getName());
        }
        if (Objects.nonNull(jsonTestCaseStep.getStopOnError())) {
            setStopOnError(jsonTestCaseStep.getStopOnError().equalsIgnoreCase("true"));
        } else if (runMethod.isAnnotationPresent(StopOnError.class)) {
            setStopOnError(runMethod.getDeclaredAnnotation(StopOnError.class).value());
        } else {
            setStopOnError(PropertyResolver.isStopOnErrorEnabled());
        }
        setSkipOnError(runMethod.isAnnotationPresent(SkipOnError.class));
    }

    /**
     * override startNow execution method of junit testcase class
     */
    public void run() {
        TestStepMonitor.setCurrentStep(this);
        logStepInfo("Step Start: " + testStepResult.getName());
        if (TestStepMonitor.isStop() || noRun) {
            noRun();
        } else {
            try {
                //prepare step
                beforeStep();
                testStepResult.setTestMethod(runMethod.getName());
                //staring handling parameters
                int parameterCount = runMethod.getParameterCount();
                if (parameterCount == 0) {//case no parameter required
                    invokeMethod(runMethod, pageObject); //invoke directly
                } else {//case parameter required
                    String using = getUsingFromJsonTestcaseStep();
                    if (parameterCount == 1) {// single param required
                        Object parameter = getParameterFromDataContainer(using);
                        // When the test step needs a JsonNode parameter, call it and leave this code block section
                        Type[] genericParameterTypes = runMethod.getParameterTypes();
                        if (isJsonNodeParameter(genericParameterTypes[0])) {
                            invokeThis(parameter);
                            this.parameters.put(using, parameter);
                        } else {
                            //start casing param type
                            invokeMethodeAfterConvertParameter(parameter, using);
                        }
                    } else {// multi-param required
                        Object[] parameters;
                        //String Array zerlegen
                        if (using.contains("[") || using.contains(",")) {
                            parameters = extractAndProcessParameters(using);
                            invokeThis(parameters);
                        } else {
                            Object parameter = getParameterFromDataContainer(using);
                            if (parameter instanceof ArrayNode) {
                                parameters = mapper.convertValue(parameter, new TypeReference<>() {
                                });
                                this.parameters.put(using, ((ArrayNode) parameter).toPrettyString());
                                invokeThis(parameters);
                            } else {
                                throw new ExceptionBase(ExceptionErrorKeys.TEST_DATA_NOT_MATCH);
                            }
                        }
                    }
                }
            } catch (Throwable throwable) {
                Throwable issue = throwable;
                if (issue instanceof InvocationTargetException) {
                    issue = ((InvocationTargetException) issue).getTargetException();
                } else if (issue instanceof TestDataEmptyException) {
                    setSkipOnError(true);
                }
                if (isSkipOnError()) {
                    testStepResult.setStatus(TestStatus.SKIPPED);
                } else {
                    testStepResult.setStatus(TestStatus.FAIL);
                }
                testStepResult.setActual("Exception: " + issue.getMessage());
                testStepResult.setTestFailure(new TestFailure(issue));
                noRun = isStopOnError() || isSkipOnError();
                TestStepMonitor.setIsStop(noRun);
            }

        }
        testStepResult.stopNow();
        logStepInfo("Step End: " + getName());
        logStepInfo("Start after step Process...");
        afterStep();
    }

    /**
     * handle parameter after read from test data container
     *
     * @param parameter parameter object
     * @param using     using definition
     */
    private void invokeMethodeAfterConvertParameter(Object parameter, String using) throws InvocationTargetException, IllegalAccessException {
        if (parameter instanceof ArrayNode arrayNode) {// Array Node
            Iterator<JsonNode> elements = arrayNode.elements();
            JsonNode elementNode = elements.next();
            invokeMethodeBasedOnNodeContent(using, arrayNode, elementNode);
        } else {//Object Node
            invokeMethodeWithObjectNode(using, parameter);
        }
    }

    /**
     * extract multiple parameters from using definition and process them
     *
     * @param using using definition
     * @return array of parameters
     */
    private Object[] extractAndProcessParameters(String using) {
        Object[] parameters;
        String[] usingKeys = using.replace("[", "").replace("]", "").split(",");
        parameters = new Object[usingKeys.length];
        for (int index = 0; index < usingKeys.length; index++) {
            //Key Werte welche im Array mitgegeben werden nun aus dem TestdataContainer auslesen
            Object valueObject = getParameterFromDataContainer(usingKeys[index].trim());

            //So kann eine Methode beide Wert haben also einen JSONNode oder eine List Definition
            Type[] genericParameterTypes = runMethod.getParameterTypes();
            if (isJsonNodeParameter(genericParameterTypes[index])) {
                parameters[index] = valueObject;
            } else {
                parameters[index] = castParameter(valueObject);
            }
            this.parameters.put(usingKeys[index], parameters[index]);
        }
        return parameters;
    }

    /**
     * get parameter from test data container
     *
     * @param using using definition
     * @return parameter object
     */
    private Object getParameterFromDataContainer(String using) {
        Object parameter;
        if (using.equalsIgnoreCase("CustomizedDataMap")) {//transfer whole map as parameter
            parameter = checkIsNotNull(testDataContainer.getDataContent().getFirst());
        } else {
            parameter = checkIsNotNull(testDataContainer.getParameter(using, PARAMETER_ROW_NUM));
        }
        return parameter;
    }

    /**
     * check if parameter type is JsonNode or its subclass
     *
     * @param genericParameterType parameter type
     * @return true if parameter type is JsonNode or its subclass
     */
    private boolean isJsonNodeParameter(Type genericParameterType) {
        return genericParameterType instanceof Class<?> && JsonNode.class.isAssignableFrom((Class<?>) genericParameterType);
    }

    /**
     * get using definition from json test case step or from annotation
     *
     * @return using definition
     */
    private String getUsingFromJsonTestcaseStep() {
        String using;
        if (jsonTestCaseStep.getUsing() == null || jsonTestCaseStep.getUsing().isEmpty()) {
            //get using from annotation content
            using = runMethod.getDeclaredAnnotation(TestStep.class).using();
        } else {
            using = jsonTestCaseStep.getUsing();
        }
        if (using.isEmpty()) {//parameter required but not found
            throw new ExceptionBase(ExceptionErrorKeys.TEST_STEP_REQUIRED_PARAMETER_NOT_FOUND, getName());
        }
        return using;
    }

    /**
     * invoke method with object node parameter
     *
     * @param using     using definition
     * @param parameter parameter object
     */
    private void invokeMethodeWithObjectNode(String using, Object parameter) throws InvocationTargetException, IllegalAccessException {
        if (using.contains("@base64")) {
            parameter = PropertyResolver.decodeBase64(((TextNode) parameter).asText());
        }
        Object parameterObject = castParameter(parameter);
        parameters.put(using, parameterObject);
        invokeThis(parameterObject);
    }

    /**
     * invoke method based on array node content
     *
     * @param using       using definition
     * @param arrayNode   array node
     * @param elementNode first element of array node
     */
    private void invokeMethodeBasedOnNodeContent(String using, ArrayNode arrayNode, JsonNode elementNode) throws InvocationTargetException, IllegalAccessException {
        if (elementNode.isValueNode()) {//in case plain array, wrap to string list
            List<String> textNodeValues = mapper.convertValue(arrayNode, new TypeReference<>() {
            });
            invokeThis(textNodeValues);
            parameters.put(using, textNodeValues);
        } else {//in case object node array, wrap to map list
            List<Map<String, Object>> objectNodesValues = mapper.convertValue(arrayNode, new TypeReference<>() {
            });
            invokeThis(objectNodesValues);
            for (int i = 0; i < objectNodesValues.size(); i++) {
                parameters.put("" + (i + 1), objectNodesValues.get(i));
            }
        }
    }

    /**
     * invoke method with parameters
     *
     * @param parameter parameters
     */
    private void invokeThis(Object... parameter) throws InvocationTargetException, IllegalAccessException {
        invokeMethod(runMethod, pageObject, parameter);
    }

    private Object castParameter(Object valueObject) {
        if (valueObject instanceof TextNode textNode) {
            return textNode.asText();
        } else if (valueObject instanceof ObjectNode) {//cast to type as expected from method, exp. map
            return mapper.<Map<String, Object>>convertValue(valueObject, new TypeReference<>() {
            });
        } else if (valueObject instanceof IntNode intNode) {
            return intNode.asInt();
        } else if (valueObject instanceof DoubleNode doubleNode) {
            return doubleNode.asDouble();
        } else if (valueObject instanceof BooleanNode booleanNode) {
            return booleanNode.asBoolean();
        } else if (valueObject instanceof LongNode longNode) {
            return longNode.asLong();
        } else if (valueObject instanceof ArrayNode arrayNode) {//cast to type as expected from method, exp. list
            return mapper.<List<String>>convertValue(arrayNode, new TypeReference<>() {
            });
        } else if (String.valueOf(valueObject).matches("true|false")) {
            return Boolean.parseBoolean(valueObject.toString());
        } else {
            return valueObject;
        }
    }

    private Object checkIsNotNull(Object parameter) {
        if (Objects.isNull(parameter)) {
            throw new ExceptionBase(ExceptionErrorKeys.NULL_EXCEPTION, "Parameter for test step: " + getName());
        }
        return parameter;
    }

    /**
     * invoke method with args
     *
     * @param method   method to invoke
     * @param instance target
     * @param args     parameters
     */
    private void invokeMethod(Method method, Object instance, Object... args) throws InvocationTargetException, IllegalAccessException {
        if (args.length > 0) {
            logStepInfo("Parameters: " + Strings.join(" | ", Arrays.stream(args).map(Object::toString).toList()));
        }
        method.invoke(instance, args);
        testStepResult.setStatus(TestStatus.PASS);
    }

    /**
     * after step actions
     */
    public void afterStep() {
        testRunResult.addStepResults(testStepResult);
        handlingScreenshots();
        if (runMethod != null) {
            //handle Annotation @UndoOnError
            if (runMethod.isAnnotationPresent(UndoOnError.class) && testStepResult.getStatus().equals(TestStatus.FAIL)) {
                safeCallMethod(runMethod.getDeclaredAnnotation(UndoOnError.class).value());
            }
        }
        TestStepMonitor.processResult(testStepResult);
    }

    private void safeCallMethod(String methodName) {
        try {
            Method method = getMethodByName(methodName);
            Assertions.assertNotNull(method, "Method with name: " + methodName + " was not found in class: " + testObjectClass.getName());
            method.invoke(pageObject);
        } catch (Throwable ex) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Call Method: " + methodName + " Unsuccessful!");
        }
    }

    public void noRun() {
        testStepResult.startNow();
        testStepResult.stopNow();
        testStepResult.setStatus(TestStatus.SKIPPED);
        logStepInfo("Skip Test Step because of stop on Error or retry process!");
    }

    public String getComment() {
        return isValid(jsonTestCaseStep.getComment()) ? jsonTestCaseStep.getComment() : "";
    }

    public String[] toDesignStep() {
        String desc = "Test Object: " + jsonTestCaseStep.getTestObject() + "; " + System.lineSeparator() +
                      "Action: " + jsonTestCaseStep.getName() + "; " + System.lineSeparator() + getComment();
        String excepted = "Will be checked automatically.";
        return new String[]{desc, excepted};
    }

    public void reportResultTo(TestRunResult testRunResult) {
        this.testRunResult = testRunResult;
    }

    @Override
    public void execute() {
        run();
    }

    public boolean hasNonHeadlessAnnotation() {
        return Objects.nonNull(runMethod.getDeclaredAnnotation(NonHeadless.class));
    }

    /**
     * take screenshot after step via level definition in test case
     */
    public void handlingScreenshots() {
        if (jsonTestCaseMetaData.getTestType().equals(TestType.REST) || jsonTestCaseMetaData.getTestType().equals(TestType.APP)) {
            setTakeScreenshot(false);
            return;
        }
        if (testStepResult.getStatus().equals(TestStatus.SKIPPED)) {
            setTakeScreenshot(false);
            return;
        }
        //Mit dieser Kombination werden auch die FAIL und ERROR Situationen abgedeckt
        setTakeScreenshot(testStepResult.getStatus().equals(TestStatus.PASS)
                          == ScreenshotLevel.SUCCESS.equals(jsonTestCaseMetaData.getScreenshotLevel()));

        //Falls nicht, Prüfen, ob es auf TestStep Ebene definiert wurde
        if (!takeScreenshot) {
            setTakeScreenshot("true".equalsIgnoreCase(jsonTestCaseStep.getTakeScreenshot())
                              || runMethod.getDeclaredAnnotation(TestStep.class).takeScreenshot());
        }
        //Screenshot erstellen
        if (takeScreenshot) {
            try {
                testStepResult.setFullScreen(ScreenCapture.takeScreenShot().getScreenshotFile());
            } catch (Exception e) {
                warn("Failed to take screenshot: " + e.getMessage());
            }
        }
    }

    /**
     * fetch method name for passing to junit Testcase constructor that
     * <p>
     * can be executed by runTest()
     *
     * @param name annotation name text
     * @return the method
     */
    private Method getMethodWithAnnoName(String name) {
        //get all public methods
        Method method = null;
        Method[] methods = testObjectClass.getMethods();
        for (Method md : methods) {
            TestStep annotation = md.getDeclaredAnnotation(TestStep.class);
            if (annotation != null && annotation.name().equals(name)) {
                method = md;
                break;
            }
        }
        if (Objects.isNull(method)) {
            throw new ExceptionBase(ExceptionErrorKeys.METHOD_NOT_FOUND, name, testObjectClass.getName());
        } else {
            return method;
        }
    }

    private Method getMethodByName(String name) {
        Method method = null;
        Method[] methods = testObjectClass.getMethods();
        for (Method md : methods) {
            if (md.getName().equals(name)) {
                method = md;
                break;
            }
        }
        return method;
    }

}
