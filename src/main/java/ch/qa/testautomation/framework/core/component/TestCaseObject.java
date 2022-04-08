package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.enumerations.PropertyKey;
import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.enumerations.TestType;
import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.common.utils.AnnotationReflector;
import ch.qa.testautomation.framework.common.utils.AnnotationUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.annotations.AfterTest;
import ch.qa.testautomation.framework.core.annotations.BeforeTest;
import ch.qa.testautomation.framework.core.annotations.NonHeadless;
import ch.qa.testautomation.framework.core.annotations.TestObject;
import ch.qa.testautomation.framework.core.json.container.JSONPageConfig;
import ch.qa.testautomation.framework.core.json.container.JSONTestCase;
import ch.qa.testautomation.framework.core.json.container.JSONTestCaseStep;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.core.media.ImageHandler;
import ch.qa.testautomation.framework.core.report.ReportBuilder;
import junit.framework.TestSuite;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.*;

public class TestCaseObject extends TestSuite implements Runnable, Comparable<TestCaseObject> {

    //all target test objects class but not initialized
    private final Map<String, Class> pageObjects = new LinkedHashMap<>();
    //all test steps of test case
    private final List<TestCaseStep> steps = new LinkedList<>();
    //test data container for the test case
    private final TestDataContainer testDataContainer;
    private final JSONTestCase testCase;
    private TestStepMonitor testStepMonitor;
    private final TestRunResult testRunResult;
    private final String parentFolderName;
    private String testCaseId;
    private String seriesNumber;
    private final String description;

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
        } catch (IOException | NoSuchFieldException | IllegalAccessException ex) {
            warn("Test Case Object initialization failed: " + jsonTestCase.getName());
            error(ex);
        }
        testDataContainer = new TestDataContainer(testCase.getTestDataRef(), testCase.getAdditionalTestDataFile());
        initTestSteps(testCase.getSteps());
        testRunResult = new TestRunResult();
        testRunResult.setDescription(description);
        parentFolderName = testCase.getPackage();
        if (testCase.getSeriesNumber() != null && !testCase.getSeriesNumber().isEmpty()) {
            seriesNumber = testCase.getSeriesNumber();
        }
        if (testCase.getTestCaseId() != null) {
            setTestCaseId(testCase.getTestCaseId());
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
            warn("Test Case Object initialization failed: " + jsonTestCase.getName());
            error(e);
        }
        testDataContainer = new TestDataContainer(testDataContent, testCase.getAdditionalTestDataFile());
        initTestSteps(testCase.getSteps());
        testRunResult = new TestRunResult();
        testRunResult.setDescription(description);
        parentFolderName = jsonTestCase.getPackage();
        if (testDataContent.get(0).get("seriesNumber") != null && !testDataContent.get(0).get("seriesNumber").toString().isEmpty()) {
            seriesNumber = testDataContent.get(0).get("seriesNumber").toString();
        }
        if (testCase.getTestCaseId() != null) {
            setTestCaseId(testCase.getTestCaseId());
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
            throw new RuntimeException("Page Object " + pageObjectNames + " was not found in implementation!!");
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
            if (pageObjects.get(jStep.getTestObject()) == null) {
                throw new RuntimeException(jStep.getTestObject() + "was not found!");
            }
            TestCaseStep testCaseStep = new TestCaseStep(i + 1, testCase.getScreenshotLevel(),
                    jStep, pageObjects.get(jStep.getTestObject()), testDataContainer);
            steps.add(testCaseStep);
            addTest(testCaseStep);
        }
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
            if (testStepResult.getStatus().equals(TestStatus.FAIL)) {
                if (PropertyResolver.isRetryEnabled()) {
                    TestRunManager.storeRetryStep(getPackage(), getName(), testCaseStep.getOrderNumber());
                    TestRunManager.storeSessions(DriverManager.getWebDriver());
                }
                stop = testCaseStep.isStopOnError();
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


    /**
     * after test
     */
    private void afterTest() {
        testStepMonitor.afterTest(); // finish last test step and also the test case
        log("INFO", "Finish Test Case: {}", getName());
        invokeWithAnnotation(AfterTest.class);
        testRunResult.stopNow("Test Case Ends: " + getName());
        //default after test: build Report
        ReportBuilder.stopRecordingTest(testRunResult);
        ImageHandler.finishVideoRecording(testRunResult);
        if (PropertyResolver.isTFSSyncEnabled()) {
            TestRunManager.tfsFeedback(Collections.singletonList(this));
        }
        if (PropertyResolver.isJIRAConnectEnabled()) {
            TestRunManager.jiraFeedback(Collections.singletonList(this));
        }
        log("TRACE", "Generate Allure Result: {}", getName());
        //generate allure results files for this test case
        ReportBuilder.generateReport(Collections.singletonList(this));
        if (PropertyResolver.isAllureReportService()) {
            TestRunManager.uploadSingleTestRunReport();
        }
        //restart driver
        if (PropertyResolver.isRestartDriverAfterExecution()) {
            trace("Driver closing ... ");
            DriverManager.closeDriver();
        }
    }

    /**
     * before test
     */
    private void beforeTest() {
        //check @NonHeadless Annotation to reset driver option
        checkNonHeadlessMethod();
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
            } else if (System.getProperty("startURL") != null) {
                DriverManager.openUrl(System.getProperty("startURL"));
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

    private void checkNonHeadlessMethod() {
        //preset non-headless method not exists
        PropertyResolver.setProperty(PropertyKey.METHOD_NONHEADLESS_EXISTS.key(), "false");
        for (Class aClass : pageObjects.values()) {
            List<Method> methods = AnnotationReflector.getAnnotatedMethods(aClass, NonHeadless.class);
            if (!methods.isEmpty()) {
                PropertyResolver.setProperty(PropertyKey.METHOD_NONHEADLESS_EXISTS.key(), "true");
                break;
            }
        }
    }
}
