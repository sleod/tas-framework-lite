package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.enumerations.TestStatus;
import ch.qa.testautomation.tas.common.enumerations.TestType;
import ch.qa.testautomation.tas.common.logging.ScreenCapture;
import ch.qa.testautomation.tas.common.logging.Screenshot;
import ch.qa.testautomation.tas.common.logging.SystemLogger;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.annotations.*;
import ch.qa.testautomation.tas.core.json.container.JSONTestCaseStep;
import ch.qa.testautomation.tas.core.json.container.JsonTestCaseMetaData;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import com.beust.jcommander.Strings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.logStepInfo;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;

public class TestCaseStep implements Executable {
    /**
     * -- GETTER --
     *
     * @return order number of step
     */
    @Getter
    private final int orderNumber;
    private final Class<?> testObjectClass;
    private final TestDataContainer testDataContainer;
    private final String screenshotLevel;
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
    private final ObjectMapper mapper = new ObjectMapper();
    @Getter
    private final String name;
    private TestRunResult testRunResult;
    private final Method runMethod;
    private boolean noRun = false;

    public TestCaseStep(int orderNumber, JsonTestCaseMetaData jsonTestCaseMetaData, JSONTestCaseStep jStep, Class<?> testObjectClass, TestDataContainer testDataContainer) {
        this.name = ("Step " + orderNumber + ". " + testObjectClass.getDeclaredAnnotation(TestObject.class)
                .name() + "@" + jStep.getName());
        this.jsonTestCaseStep = jStep;
        this.testDataContainer = testDataContainer;
        this.orderNumber = orderNumber;
        this.testObjectClass = testObjectClass;
        this.screenshotLevel = jsonTestCaseMetaData.getScreenshotLevel();
        this.testStepResult = new TestStepResult(getName(), getOrderNumber());
        //get method with annotated name
        this.runMethod = getMethodWithAnnoName(jsonTestCaseStep.getName());
        this.jsonTestCaseMetaData = jsonTestCaseMetaData;
    }

    public String prepareAndGetDisplayName(boolean retry, int retryOrder) {
        noRun = PropertyResolver.isRetryOnErrorEnabled() && retry
                && retryOrder >= 0 && retryOrder >= getOrderNumber() + PropertyResolver.getRetryOverSteps();
        return getName();
    }

    public boolean isTakeScreenshotDefinedOnTestcaseStep() {
        if (!takeScreenshot && jsonTestCaseStep.getTakeScreenshot() != null) {
            setTakeScreenshot(jsonTestCaseStep.getTakeScreenshot().equalsIgnoreCase("true"));
        }
        return takeScreenshot;
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
            pageObject = getInstanceOf(testObjectClass);
            testDataContainer.addPageObject(testObjectClass.getName(), pageObject);
        } else {
            pageObject = testDataContainer.getPageObject(testObjectClass.getName());
        }
        //override take screenshot setting upon method
        setTakeScreenshot(runMethod.getDeclaredAnnotation(TestStep.class).takeScreenshot());
        if (jsonTestCaseStep.getStopOnError() != null) {
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
        SystemLogger.setCurrTestStepResult(testStepResult);
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
                    String using;
                    if (jsonTestCaseStep.getUsing() == null || jsonTestCaseStep.getUsing().isEmpty()) {
                        using = runMethod.getDeclaredAnnotation(TestStep.class).using();
                    } else {
                        using = jsonTestCaseStep.getUsing();
                    }
                    if (using.isEmpty()) {//parameter required but not found
                        throw new ExceptionBase(ExceptionErrorKeys.TEST_STEP_REQUIRED_PARAMETER_NOT_FOUND, getName());
                    }
                    int parameterRowNum = 0;
                    if (parameterCount == 1) {// single param required
                        Object parameter;
                        if (using.equalsIgnoreCase("CustomizedDataMap")) {//transfer whole map as parameter
                            parameter = secureParameter(testDataContainer.getDataContent().get(parameterRowNum));
                        } else {
                            parameter = secureParameter(testDataContainer.getParameter(using, parameterRowNum));
                        }
                        //start casing param type
                        if (parameter instanceof ArrayNode arrayNode) {// Array Node
                            Iterator<JsonNode> elements = arrayNode.elements();
                            JsonNode elementNode = elements.next();
                            if (elementNode.isValueNode()) {//in case plain array, wrap to string list
                                List<String> textNodeValues = mapper.convertValue(arrayNode, new TypeReference<>() {
                                });
                                invokeMethod(runMethod, pageObject, textNodeValues);
                            } else {//in case object node array, wrap to map list
                                List<Map<String, Object>> objectNodesValues = mapper.convertValue(arrayNode, new TypeReference<>() {
                                });
                                invokeMethod(runMethod, pageObject, objectNodesValues);
                            }
                        } else {//Object Node
                            if (using.contains("@base64")) {
                                parameter = PropertyResolver.decodeBase64(((TextNode) parameter).asText());
                            }
                            invokeMethod(runMethod, pageObject, castParameter(parameter));
                        }
                    } else {// multi-param required
                        Object[] parameters;
                        //String Array zerlegen
                        if (using.contains("[") || using.contains(",")) {
                            String[] usingKeys = using.replace("[", "").replace("]", "").split(",");
                            parameters = new Object[usingKeys.length];
                            for (int index = 0; index < usingKeys.length; index++) {
                                //Key Werte welche im Array mitgegeben werden nun aus dem TestdataContainer auslesen
                                Object valueObject = secureParameter(testDataContainer.getParameter(usingKeys[index].trim(), parameterRowNum));
                                parameters[index] = castParameter(valueObject);
                            }
                            invokeMethod(runMethod, pageObject, parameters);
                        } else {
                            Object parameter = secureParameter(testDataContainer.getParameter(using, parameterRowNum));
                            if (parameter instanceof ArrayNode) {
                                parameters = mapper.convertValue(parameter, new TypeReference<>() {
                                });
                                invokeMethod(runMethod, pageObject, parameters);
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
        afterStep();
    }

    private Object secureParameter(Object parameter) {
        if (Objects.isNull(parameter)) {
            throw new ExceptionBase(ExceptionErrorKeys.NULL_EXCEPTION, "Parameter of Test Step: " + getName());
        }
        return parameter;
    }

    private Object castParameter(Object valueObject) {
        if (valueObject instanceof TextNode textNode) {
            return textNode.asText();
        } else if (valueObject instanceof ObjectNode) {
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
        } else if (valueObject instanceof ArrayNode arrayNode) {
            return mapper.<List<String>>convertValue(arrayNode, new TypeReference<>() {
            });
        } else if (String.valueOf(valueObject).matches("true|false")) {
            return Boolean.parseBoolean(valueObject.toString());
        } else {
            return valueObject;
        }
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
            SystemLogger.logStepInfo("Parameters: " + Strings.join(" | ", Arrays.stream(args).map(Object::toString)
                    .collect(Collectors.toList())));
        }
        method.invoke(instance, args);
        testStepResult.setStatus(TestStatus.PASS);
    }

    /**
     * instance test object
     *
     * @param testObjectClass test object class
     * @return instance of test object
     * @throws IllegalAccessException exception
     * @throws InstantiationException exception
     */
    private Object getInstanceOf(Class<?> testObjectClass) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        return testObjectClass.getDeclaredConstructor().newInstance();
    }

    /**
     * after step actions
     */
    public void afterStep() {
        testRunResult.addStepResults(testStepResult);
        logStepInfo("Step End: " + testStepResult.getName());
        handlingScreenshots();
        //handle Annotation @UndoOnError
        if (runMethod != null && runMethod.isAnnotationPresent(UndoOnError.class) && testStepResult.getStatus()
                .equals(TestStatus.FAIL)) {
            String methodName = runMethod.getDeclaredAnnotation(UndoOnError.class).value();
            try {
                Method undo = getMethodUndoMethod(methodName);
                Assertions.assertNotNull(undo, "Undo Method with name: " + methodName + " was not found in class: " + testObjectClass.getName());
                undo.invoke(pageObject);
            } catch (Throwable ex) {
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Undo Step Unsuccessful!");
            }
        }
        TestStepMonitor.processResult(testStepResult);
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
    private void handlingScreenshots() {

        if (jsonTestCaseMetaData.getTestType().equals(TestType.REST)) {
            this.takeScreenshot = false;
        } else {
            Screenshot screenshot = null;
            if (screenshotLevel.equalsIgnoreCase("SUCCESS") || isTakeScreenshotDefinedOnTestcaseStep()) {
                this.takeScreenshot = true;
            } else {
                if (screenshotLevel.equalsIgnoreCase("ERROR")) {
                    this.takeScreenshot = testStepResult.getStatus().equals(TestStatus.FAIL);
                }
            }
            if (this.takeScreenshot) {
                screenshot = ScreenCapture.takeScreenShot();
            }
            if (screenshot != null) {
                testStepResult.addScreenshot(screenshot);
            }
        }
    }

    /**
     * fetch method name for passing to junit Testcase constructor that
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

    private Method getMethodUndoMethod(String name) {
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
