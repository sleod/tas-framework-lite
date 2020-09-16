package ch.sleod.testautomation.framework.core.component;

import ch.sleod.testautomation.framework.common.enumerations.TestStatus;
import ch.sleod.testautomation.framework.common.enumerations.TestType;
import ch.sleod.testautomation.framework.common.logging.SystemLogger;
import ch.sleod.testautomation.framework.common.utils.AnnotationReflector;
import ch.sleod.testautomation.framework.common.utils.AnnotationUtils;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.annotations.AfterTest;
import ch.sleod.testautomation.framework.core.annotations.BeforeTest;
import ch.sleod.testautomation.framework.core.annotations.TestObject;
import ch.sleod.testautomation.framework.core.json.container.JSONPageConfig;
import ch.sleod.testautomation.framework.core.json.container.JSONTestCase;
import ch.sleod.testautomation.framework.core.json.container.JSONTestCaseStep;
import ch.sleod.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.sleod.testautomation.framework.core.media.ImageHandler;
import ch.sleod.testautomation.framework.core.report.ReportBuilder;
import junit.framework.TestSuite;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.*;

public class TestCaseObject extends TestSuite implements Runnable, Comparable<TestCaseObject> {

    //all target test objects class but not initialized
    private Map<String, Class> pageObjects = new LinkedHashMap<>();
    //all test steps of test case
    private List<TestCaseStep> steps = new LinkedList<>();
    //test data container for the test case
    private TestDataContainer testDataContainer;
    private final JSONTestCase testCase;
    private TestStepMonitor testStepMonitor;
    private TestRunResult testRunResult;
    private String parentFolderName;
    private String testCaseId;
    private String seriesNumber;
    private String description;

    /**
     * create test case object with json test case object
     *
     * @param jsonTestCase deserialize json test case
     */
    public TestCaseObject(JSONTestCase jsonTestCase) {
        super(jsonTestCase.getName());
        testCase = jsonTestCase;
        this.description = testCase.getDescription();
        try {
            initTestObjects(testCase.getTestObjectNames());
            initTestData(testCase.getTestDataRef());
            initTestSteps(testCase.getSteps());
            testRunResult = new TestRunResult();
            testRunResult.setDescription(description);
            parentFolderName = testCase.getPackage();
            if (testCase.getSeriesNumber() != null && !testCase.getSeriesNumber().isEmpty()) {
                seriesNumber = testCase.getSeriesNumber();
            }
        } catch (IOException | NoSuchFieldException | IllegalAccessException ex) {
            error(ex);
        }
    }

    /**
     * create test case object with json test case object
     *
     * @param jsonTestCase       deserialize json test case
     * @param testDataContent    test data content
     * @param optionalNameSuffix optional test case name suffix for variation or number
     */
    public TestCaseObject(JSONTestCase jsonTestCase, List<Map<String, Object>> testDataContent, String optionalNameSuffix) {
        super(jsonTestCase.getName() + optionalNameSuffix);
        testCase = jsonTestCase;
        this.description = testCase.getDescription();
        try {
            initTestObjects(testCase.getTestObjectNames());
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            error(e);
        }
        initTestData(testDataContent);
        initTestSteps(testCase.getSteps());
        testRunResult = new TestRunResult();
        testRunResult.setDescription(description);
        parentFolderName = jsonTestCase.getPackage();
        if (testDataContent.get(0).get("seriesNumber") != null && !testDataContent.get(0).get("seriesNumber").toString().isEmpty()) {
            seriesNumber = testDataContent.get(0).get("seriesNumber").toString();
        }
    }

    /**
     * init all test object with annotated name
     *
     * @param pageObjectNames annotation name
     * @throws RuntimeException case no test objects found
     */
    public void initTestObjects(List<String> pageObjectNames) throws RuntimeException, IOException, NoSuchFieldException, IllegalAccessException {
        String defaultTestAutomationPackage = PropertyResolver.getDefaultTestautomationPackage();
        TestType testType = TestType.valueOf(testCase.getType().toUpperCase());
        for (Class<?> clazz : AnnotationReflector.getAnnotatedClass(defaultTestAutomationPackage, TestObject.class)) {
            TestObject annotation = clazz.getAnnotation(TestObject.class);
            if (annotation.name().isEmpty()) {//to avoid class inherit with annotation but not specified in own class
                SystemLogger.warn(clazz + " has inherited Annotation but not specified with own name. And this class will not be initialized!!");
                continue;
            }
            String pageObjectName = annotation.name();
            if (pageObjectNames.contains(pageObjectName)) {
                String config = annotation.definedBy();
                //override findBy content via config file
                updatePageObjectClass(clazz, config, testType);
                pageObjects.put(pageObjectName, clazz);
            }
        }
        if (pageObjects.isEmpty()) {
            throw new RuntimeException("Page Object " + pageObjectNames + " was found in implementation!!");
        }
    }

    /**
     * init test steps
     *
     * @param jSteps json test case steps
     */
    @SuppressWarnings("unchecked")
    public void initTestSteps(List<JSONTestCaseStep> jSteps) {
        for (int i = 0, jStepsSize = jSteps.size(); i < jStepsSize; i++) {
            JSONTestCaseStep jStep = jSteps.get(i);
            TestCaseStep testCaseStep = new TestCaseStep(i + 1, testCase.getScreenshotLevel(),
                    jStep, pageObjects.get(jStep.getTestObject()), testDataContainer);
            steps.add(testCaseStep);
            addTest(testCaseStep);
        }
    }

    /**
     * init test data container for loading test data
     *
     * @param dataContent test data
     */
    public void initTestData(List<Map<String, Object>> dataContent) {
        testDataContainer = new TestDataContainer(dataContent);
    }

    /**
     * init test data container for loading test data
     *
     * @param testDataRef test data reference
     */
    public void initTestData(String testDataRef) {
        if (testDataRef == null || testDataRef.isEmpty()) {
            SystemLogger.error(new RuntimeException("Nn test case: " + getName() + " -> No Test Data for Test Object defined!"));
            System.exit(-1);
        }
        testDataContainer = new TestDataContainer(testDataRef);
    }

    /**
     * Main Method to run the test
     */
    @Override
    public void run() {
        beforeTest(); // do something before test run like preparing ...
        boolean stop = false;
        int retryStepOrder = -1;
        boolean testCaseToRetry = false;
        if (PropertyResolver.isRetryEnabled()) { // prepare retry process
            retryStepOrder = TestRunManager.loadRetryStepOrder();
            testCaseToRetry = TestRunManager.loadRetryTestCaseID().equals(getPackage() + "." + getName());
            TestRunManager.restoreSessions(DriverManager.getWebDriver());
        }
        for (TestCaseStep testCaseStep : steps) { // run every step
            TestStepResult testStepResult = new TestStepResult(testCaseStep.getName(), testCaseStep.getOrderNumber(), getObjectId());
            testStepMonitor.beforeStep(testStepResult.getStepId(), testStepResult);
            testCaseStep = testCaseStep.useTestStepMonitor(testStepMonitor).useStepResult(testStepResult);
            if (!stop) {
                if (!PropertyResolver.isRetryEnabled() || retryStepOrder < 0) {
                    testCaseStep.run();
                } else {
                    if (testCaseToRetry && retryStepOrder <= testCaseStep.getOrderNumber() + PropertyResolver.getRetryOverSteps()) {
                        testCaseStep.run();
                    } else {
                        testCaseStep.noRun();
                    }
                }
            } else { // skip test step
                testCaseStep.noRun();
            }
            // stop test on error
            if (testStepResult.getStatus().equals(TestStatus.FAIL) && PropertyResolver.stopRunOnError()) {
                if (PropertyResolver.isRetryEnabled()) {
                    TestRunManager.storeRetryStep(getPackage(), getName(), testCaseStep.getOrderNumber());
                    TestRunManager.storeSessions(DriverManager.getWebDriver());
                }
                if (PropertyResolver.getStopOnErrorPerStep()) {
                    stop = true;
                }
            }
            testRunResult.addStepResults(testStepResult);
        }
        afterTest(); // finish test run
    }

    /**
     * using test step monitor
     *
     * @param testStepMonitor test step monitor
     */
    public void useTestStepMonitor(TestStepMonitor testStepMonitor) {
        this.testStepMonitor = testStepMonitor;
    }

    public TestRunResult getTestRunResult() {
        return testRunResult;
    }

    public List<TestCaseStep> getSteps() {
        return steps;
    }

    public String getPackage() {
        return parentFolderName;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public int countTestCases() {
        return steps.size();
    }

    public TestDataContainer getTestDataContainer() {
        return testDataContainer;
    }

    public JSONTestCase getTestCase() {
        return testCase;
    }

    public String getObjectId() {
        if (PropertyResolver.isObjectIdEnabled()) {
            return "{" + getName() + "}[" + System.identityHashCode(this) + "]";
        } else {
            return "{" + getName() + "}";
        }
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    /**
     * after test
     */
    private void afterTest() {
        testStepMonitor.afterTest(); // finish test step
        log("INFO", "Finish Test Case: {}", getName());
        invokeWithAnnotation(AfterTest.class);
        testRunResult.stopNow("Test Case Ends: " + getName());
        //default after test: build Report
        ReportBuilder.stopRecordingTest(testRunResult);
        ImageHandler.finishVideoRecording(testRunResult);
        if (PropertyResolver.isTFSFeedbackEnabled() && TestRunManager.feedbackAfterSingleTest()) {
            TestRunManager.tfsFeedback(Collections.singletonList(this));
        }
    }

    /**
     * before test
     */
    private void beforeTest() {
        TestRunManager.loadDriver(getTestCase(), getName());
        testRunResult.setName(getObjectId());
        ImageHandler.prepareVideoRecording(testRunResult);
        testStepMonitor.beforeTest(getObjectId()); // prepare test step monitor
        testRunResult.startNow("Test Case Begin: " + getName());
        log("INFO", "Start Test Case: {}", getName());
        invokeWithAnnotation(BeforeTest.class);
        if (testCase.getType().contains("web_app")) {
            String url = testCase.getStartURL();
            if (!url.isEmpty()) {
                DriverManager.openUrl(url);
            }
        }
        ReportBuilder.startRecordingTest(testRunResult);
    }

    /**
     * invoke method with given annotation
     *
     * @param annotation annotation of method
     */
    private void invokeWithAnnotation(Class<? extends Annotation> annotation) {
        List<Method> methods = AnnotationReflector.getAnnotatedMethods(TestRunManager.getPerformer().getClass(), annotation);
        if (!methods.isEmpty()) {
            if (methods.size() > 1) {
                throw new RuntimeException("Method with Annotation " + annotation + " should only be defined once globally!");
            } else {
                try {
                    methods.get(0).invoke(TestRunManager.getPerformer());
                } catch (IllegalAccessException ex) {
                    error(ex);
                } catch (InvocationTargetException ex) {
                    error(ex.getTargetException());
                }
            }
        }
    }

    /**
     * override and update class annotation content via config
     *
     * @param clazz    class of annotation
     * @param config   file of config of test object
     * @param testType test type
     * @throws IOException            io exception
     * @throws NoSuchFieldException   field exception
     * @throws IllegalAccessException access exception
     */
    private void updatePageObjectClass(Class<?> clazz, String config, TestType testType) throws IOException, NoSuchFieldException, IllegalAccessException {
        if (!config.isEmpty()) {
            String configLocation = PropertyResolver.getDefaultPageConfigLocation();
            JSONPageConfig jsonPageConfig = JSONContainerFactory.buildTestObjectConfig(configLocation + config);
            LinkedHashSet<String> declaredFieldsNames = new LinkedHashSet<>();
            for (Field field : clazz.getDeclaredFields()) {
                declaredFieldsNames.add(field.getName());
            }
            //override findBy with page object config in json
            for (Map.Entry<String, Map<String, String>> entry : jsonPageConfig.getConfigurations().entrySet()) {
                String fieldName = entry.getKey();
                Map<String, String> value = entry.getValue();
                if (declaredFieldsNames.contains(fieldName)) {
                    Field field = clazz.getDeclaredField(fieldName);
                    if (testType.equals(TestType.WEB_APP) || testType.equals(TestType.MOBILE_WEB_APP)) {
                        AnnotationUtils.changeFindByValue(field, value);
                    } else if (testType.equals(TestType.MOBILE_ANDROID)) {
                        AnnotationUtils.changeAndroidFindByValue(field, value);
                    } else if (testType.equals(TestType.MOBILE_IOS)) {
                        AnnotationUtils.changeIOSFindByValue(field, value);
                    }
                }
            }
        }
    }

    @Override
    public int compareTo(TestCaseObject tco) {
        int result = 0;
        String keyIn = tco.getSeriesNumber();
        String keyThis = this.getSeriesNumber();
        if (keyIn != null && !keyIn.isEmpty() && keyThis != null && !keyThis.isEmpty()) {
            try {
                int orderIn = Integer.parseInt(keyIn.substring(keyIn.lastIndexOf(".") + 1));
                int orderThis = Integer.parseInt(keyThis.substring(keyThis.lastIndexOf(".") + 1));
                result = orderThis - orderIn;
            } catch (NumberFormatException ex) {
                warn("Order Number has Wrong format: " + keyThis + ", " + keyIn);
                error(ex);
            }
        }
        return result;
    }

    public String getDescription() {
        return this.description;
    }
}
