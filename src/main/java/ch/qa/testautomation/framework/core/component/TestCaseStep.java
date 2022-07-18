package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.logging.ScreenCapture;
import ch.qa.testautomation.framework.common.logging.Screenshot;
import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.annotations.AndroidActivity;
import ch.qa.testautomation.framework.core.annotations.StopOnError;
import ch.qa.testautomation.framework.core.annotations.TestObject;
import ch.qa.testautomation.framework.core.annotations.TestStep;
import ch.qa.testautomation.framework.core.assertion.KnownIssueException;
import ch.qa.testautomation.framework.core.json.container.JSONTestCaseStep;
import junit.framework.TestCase;
import net.sf.json.JSONArray;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;


public class TestCaseStep extends TestCase {
    private final int orderNumber;
    private final Class<?> testObjectClass;
    private final TestDataContainer testDataContainer;
    private TestStepMonitor testStepMonitor;
    private final String screenshotLevel;
    private Object instance = null;
    private final JSONTestCaseStep jsonTestCaseStep;
    private TestStepResult testStepResult;
    private boolean takeScreenshot = false;
    private boolean stopOnError = false;


    public TestCaseStep(int orderNumber, String screenshotLevel, JSONTestCaseStep jStep, Class<TestObject> testObjectClass, TestDataContainer testDataContainer) {
        super("Step " + orderNumber + ". " + testObjectClass.getDeclaredAnnotation(TestObject.class).name() + "@" + jStep.getName());
        this.jsonTestCaseStep = jStep;
        this.testDataContainer = testDataContainer;
        this.orderNumber = orderNumber;
        this.testObjectClass = testObjectClass;
        this.screenshotLevel = screenshotLevel;
    }

    /**
     * @return order number of step
     */
    public int getOrderNumber() {
        return orderNumber;
    }

    public TestCaseStep useTestStepMonitor(TestStepMonitor testStepMonitor) {
        this.testStepMonitor = testStepMonitor;
        return this;
    }

    public TestCaseStep useStepResult(TestStepResult result) {
        this.testStepResult = result;
        return this;
    }

    public TestStepResult getTestStepResult() {
        return testStepResult;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    public boolean isTakeScreenshotDefinedOnTestcaseStep() {
        if (!takeScreenshot && jsonTestCaseStep.getTakeScreenshot() != null) {
            setTakeScreenshot(jsonTestCaseStep.getTakeScreenshot().equalsIgnoreCase("true"));
        }
        return takeScreenshot;
    }

    public void setTakeScreenshot(boolean takeScreenshot) {
        this.takeScreenshot = takeScreenshot;
    }

    /**
     * override startNow execution method of junit testcase class
     */
    @Override
    protected void runTest() {
        testStepResult.startNow();
        String fName = getName();
        assertNotNull("TestCase.fName cannot be null", fName); // Some VMs crash when calling getMethod(null,null);
        Method runMethod;
        try {
            //get method with annotated name
            runMethod = getMethodWithAnnoName(testObjectClass, jsonTestCaseStep.getName());
            if (runMethod == null) {
                throw new RuntimeException("No Such Method found! Annotation: " + TestStep.class.getName() + ", name: " + jsonTestCaseStep.getName());
            }
            if (!Modifier.isPublic(runMethod.getModifiers())) {
                throw new RuntimeException("Method \"" + fName + "\" should be public");
            }
            //create and store instance if not exists
            if (testDataContainer.getInstances().get(testObjectClass) == null) {
                testDataContainer.getInstances().put(testObjectClass, getInstanceOf(testObjectClass));
            }
            //override take screenshot setting upon method
            setTakeScreenshot(runMethod.getDeclaredAnnotation(TestStep.class).takeScreenshot());
            if (jsonTestCaseStep.getStopOnError() != null) {
                setStopOnError(jsonTestCaseStep.getStopOnError().equalsIgnoreCase("true"));
            } else if (runMethod.getDeclaredAnnotation(StopOnError.class) != null) {
                setStopOnError(runMethod.getDeclaredAnnotation(StopOnError.class).value());
            } else {
                setStopOnError(PropertyResolver.stopRunOnError());
            }
            //retrieve instance from storage
            instance = testDataContainer.getInstances().get(testObjectClass);
            testStepResult.setTestMethod(runMethod.getName());
            beforeStep();
            //staring handling parameters
            int parameterCount = runMethod.getParameterCount();
            if (parameterCount == 0) {//case no parameter required
                invokeMethod(runMethod, instance); //invoke directly
            } else {//case parameter required
                String using;
                if (jsonTestCaseStep.getUsing() == null || jsonTestCaseStep.getUsing().isEmpty()) {
                    using = runMethod.getDeclaredAnnotation(TestStep.class).using();
                } else {
                    using = jsonTestCaseStep.getUsing();
                }
                if (using.isEmpty()) {//parameter required but not found
                    String message = "Parameter \"using\" is not defined as same as required for method: " + runMethod.getName();
//                    fail(message);
                    testStepMonitor.failed(testStepResult.getStepId(), new RuntimeException(message), isStopOnError());
                    throw new RuntimeException(message);
                }
                int parameterRowNum = 0;
                if (parameterCount == 1) {
                    Object parameter;
                    if (using.equalsIgnoreCase("CustomizedDataMap")) {
                        //transfer whole map as parameter
                        parameter = testDataContainer.getDataContent().get(parameterRowNum);
                    } else {
                        parameter = testDataContainer.getParameter(using, parameterRowNum);
                    }
                    if (parameter instanceof JSONArray) {
                        invokeMethod(runMethod, instance, Arrays.asList(((JSONArray) parameter).toArray()));
                    } else {
                        if (using.length() > 7 && using.substring(using.length() - 7).equalsIgnoreCase("@base64")) {
                            parameter = PropertyResolver.decodeBase64(parameter.toString());
                        }
                        invokeMethod(runMethod, instance, parameter);
                    }
                } else {
                    Object[] parameters;
                    if (using.contains("[") || using.contains(",")) {
                        String[] usingKeys = using.replace("[", "").replace("]", "").split(",");
                        parameters = new Object[usingKeys.length];
                        for (int index = 0; index < usingKeys.length; index++) {
                            parameters[index] = testDataContainer.getParameter(usingKeys[index].trim(), parameterRowNum);
                        }
                    } else {
                        Object parameter = testDataContainer.getParameter(using, parameterRowNum);
                        if (parameter instanceof JSONArray) {
                            parameters = ((JSONArray) parameter).toArray();
                        } else {
                            throw new RuntimeException("Given test data does not match to required parameters!");
                        }
                    }
                    invokeMethod(runMethod, instance, parameters);
                }
            }
        } catch (Throwable throwable) {
            if (throwable instanceof InvocationTargetException) {
                throwable = ((InvocationTargetException) throwable).getTargetException();
            }
            if (throwable instanceof KnownIssueException) {
                testStepResult.setStatus(TestStatus.BROKEN);
                testStepResult.setTestFailure(new TestFailure(throwable));
                testStepMonitor.broken(testStepResult.getStepId(), throwable);
            } else {
                testStepResult.setStatus(TestStatus.FAIL);
                testStepResult.setActual("Exception: " + throwable.getMessage());
                testStepResult.setTestFailure(new TestFailure(throwable));
                testStepMonitor.failed(testStepResult.getStepId(), throwable, isStopOnError());
            }
        } finally {
            testStepResult.stopNow();
            afterStep();
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
        testStepMonitor.stepInfo(method, args);
        method.invoke(instance, args);
        testStepMonitor.succeed(testStepResult.getStepId());
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
     * before step actions
     */
    private void beforeStep() throws RuntimeException {
        if (testStepMonitor == null) {
            throw new RuntimeException("Test Step Monitor is not initialized!");
        }
        if (instance == null) {
            throw new RuntimeException("Test Object is not initialized!");
        }
    }

    /**
     * after step actions
     */
    private void afterStep() {
        handlingScreenshots();
    }

    /**
     * take screenshot after step via level definition in test case
     */
    private void handlingScreenshots() {
        Screenshot screenshot = null;
        if (screenshotLevel.equalsIgnoreCase("SUCCESS") || isTakeScreenshotDefinedOnTestcaseStep()) {
            this.takeScreenshot = true;
        } else {
            if (screenshotLevel.equalsIgnoreCase("ERROR")) {
                this.takeScreenshot = testStepResult.getStatus().equals(TestStatus.FAIL);
            }
        }
        if (this.takeScreenshot) {
            screenshot = ScreenCapture.takeScreenShot(testStepMonitor);
        }
        if (screenshot != null) {
            testStepResult.addScreenshot(screenshot);
        }
    }

    /**
     * fetch method name for passing to junit Testcase constructor that
     * can be executed by runTest()
     *
     * @param testObjectClass test object class
     * @param name            annotation name text
     * @return the method
     */
    private Method getMethodWithAnnoName(Class<?> testObjectClass, String name) {
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
        return method;
    }

    public void noRun() {
        testStepResult.startNow();
        testStepResult.stopNow();
        testStepResult.setStatus(TestStatus.SKIPPED);
        testStepMonitor.ignorable(testStepResult.getStepId());
    }

    public String getComment() {
        return jsonTestCaseStep.getComment();
    }
}
