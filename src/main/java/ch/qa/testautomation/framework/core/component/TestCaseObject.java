package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.enumerations.TestType;
import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.common.utils.AnnotationReflector;
import ch.qa.testautomation.framework.common.utils.AnnotationUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.annotations.AfterTest;
import ch.qa.testautomation.framework.core.annotations.BeforeTest;
import ch.qa.testautomation.framework.core.annotations.TestObject;
import ch.qa.testautomation.framework.core.json.container.JSONPageConfig;
import ch.qa.testautomation.framework.core.json.container.JSONTestCase;
import ch.qa.testautomation.framework.core.json.container.JSONTestCaseStep;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.core.report.ReportBuilder;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import org.junit.jupiter.api.DynamicTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public class TestCaseObject implements Comparable<TestCaseObject> {

    //all target test objects class but not initialized
    private final Map<String, Class<?>> pageObjects = new LinkedHashMap<>();
    //all test steps of test case
    private final List<TestCaseStep> steps = new LinkedList<>();
    //test data container for the test case
    private final TestDataContainer testDataContainer;
    private final JSONTestCase testCase;
    private final TestRunResult testRunResult;
    private String testCaseId;
    private Map<String, String> testCaseIdMap;
    private String seriesNumber;
    private final String description;
    private String name;
    private final String originalName;
    private String filePath;
    private final TestType type;
    private String suiteName;
    private ReportBuilder reportBuilder;

    /**
     * create test case object with json test case object
     *
     * @param jsonTestCase deserialize json test case
     */
    public TestCaseObject(JSONTestCase jsonTestCase) {
        this.name = (jsonTestCase.getName());
        this.originalName = jsonTestCase.getName();
        testCase = jsonTestCase;
        this.type = TestType.valueOf(jsonTestCase.getType().toUpperCase());
        this.description = testCase.getDescription();
        initTestObjects(testCase.getTestObjectNames());
        testDataContainer = new TestDataContainer(testCase.getTestDataRef(), testCase.getAdditionalTestDataFile());
        initTestSteps(testCase.getSteps());
        testRunResult = new TestRunResult();
        testRunResult.setDescription(description);
        setTestCaseId(testCase.getTestCaseId());
        setTestCaseIdMap(testCase.getTestCaseIdMap());
        if (testCase.getSeriesNumber() != null && !testCase.getSeriesNumber().isEmpty()) {
            seriesNumber = testCase.getSeriesNumber();
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
        this.name = (jsonTestCase.getName() + optionalNameSuffix);
        this.originalName = jsonTestCase.getName();
        testCase = jsonTestCase;
        this.type = TestType.valueOf(jsonTestCase.getType().toUpperCase());
        this.description = testCase.getDescription();
        initTestObjects(testCase.getTestObjectNames());
        testDataContainer = new TestDataContainer(testDataContent, testCase.getAdditionalTestDataFile());
        initTestSteps(testCase.getSteps());
        testRunResult = new TestRunResult();
        testRunResult.setDescription(description);
        setTestCaseId(testCase.getTestCaseId());
        setTestCaseIdMap(testCase.getTestCaseIdMap());
        if (testDataContent.get(0).get("seriesNumber") != null && !testDataContent.get(0).get("seriesNumber").toString().isEmpty()) {
            seriesNumber = testDataContent.get(0).get("seriesNumber").toString();
        }
    }

    /**
     * init all test object with annotated name
     *
     * @param pageObjectNames annotation name
     * @throws ApollonBaseException case no test objects found
     */
    public void initTestObjects(List<String> pageObjectNames) {
        String taPackageName = PropertyResolver.getTestautomationPackage();
        TestType testType = TestType.valueOf(testCase.getType().toUpperCase());
        Set<Class<?>> annotateClazz = AnnotationReflector.getAnnotatedClass(taPackageName, TestObject.class);
        if (annotateClazz.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.IMPLEMENTATION_NOT_FOUND, taPackageName);
        }
        for (Class<?> clazz : annotateClazz) {
            TestObject annotation = clazz.getAnnotation(TestObject.class);
            if (annotation.name().isEmpty()) {//to avoid class inherit with annotation but not specified in own class
                throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, clazz + " has inherited Annotation but not specified with own name. And this class will not be initialized!!");
            }
            String pageObjectName = annotation.name();
            if (pageObjectNames.contains(pageObjectName)) {
                String config = annotation.definedBy();
                //override findBy content via config file
                try {
                    updatePageObjectClass(clazz, config, testType);
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, ex, "Test Case Object initialization failed by update Annotation: " + pageObjectName);
                }
                pageObjects.put(pageObjectName, clazz);
            }
        }
        if (pageObjects.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.IMPLEMENTATION_NOT_FOUND, pageObjectNames);
        }
    }

    /**
     * init test steps
     *
     * @param jSteps json test case steps
     */
    public void initTestSteps(List<JSONTestCaseStep> jSteps) {
        for (int i = 0, jStepsSize = jSteps.size(); i < jStepsSize; i++) {
            JSONTestCaseStep jStep = jSteps.get(i);
            if (pageObjects.get(jStep.getTestObject()) == null) {
                throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, jStep.getTestObject() + "was not found!");
            }
            TestCaseStep testCaseStep = new TestCaseStep(i + 1, testCase.getScreenshotLevel(), jStep, pageObjects.get(jStep.getTestObject()), testDataContainer);
            steps.add(testCaseStep);
        }
    }

    /**
     * Main Method to run the test
     */
    public Stream<DynamicTest> getTestSteps() {
        int retryStepOrder = -1;
        boolean testCaseToRetry = false;
        if (PropertyResolver.isRetryEnabled()) {// prepare retry process
            retryStepOrder = TestRunManager.loadRetryStepOrder();
            testCaseToRetry = TestRunManager.loadRetryTestCaseID().equals(getFilePath() + "." + getName());
            TestRunManager.restoreSessions();
        }
        int finalRetryStepOrder = retryStepOrder;
        boolean finalTestCaseToRetry = testCaseToRetry;
        return steps.stream().map(step -> {
            step.reportResultTo(testRunResult);
            return DynamicTest.dynamicTest(step.prepareAndGetDisplayName(finalTestCaseToRetry, finalRetryStepOrder), step);
        });
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public String getName() {
        return name;
    }

    public TestRunResult getTestRunResult() {
        return testRunResult;
    }

    public List<TestCaseStep> getSteps() {
        return steps;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPackageName() {
        return filePath.replace("/" + FileOperation.getFileName(filePath), "");
    }

    public String getTestCaseId() {
        return Objects.isNull(testCaseId) ? "" : testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    /**
     * Only for mobile_app test case
     *
     * @return map of testcase id with key of plattform {"iOS":"id1", "android":"id2"}
     */
    public Map<String, String> getTestCaseIdMap() {
        return Objects.isNull(testCaseIdMap) ? Collections.emptyMap() : testCaseIdMap;
    }

    public void setTestCaseIdMap(Map<String, String> testCaseIdMap) {
        this.testCaseIdMap = testCaseIdMap;
    }

    public TestDataContainer getTestDataContainer() {
        return testDataContainer;
    }

    public JSONTestCase getTestCase() {
        return testCase;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public TestType getType() {
        return type;
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
                throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, ex, "Order Number has Wrong format: " + keyThis + ", " + keyIn);
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
    public void afterTest() {
        SystemLogger.info("Finish Test Case: " + getName());
        invokeWithAnnotation(AfterTest.class);
        testRunResult.stopNow("Test Case Ends: " + getName());
        //default after test: build Report
        getReportBuilder().stopRecordingTest(testRunResult);
        testDataContainer.clearPageObjects();
        if (getTestCaseId().isEmpty() && !getTestCaseIdMap().isEmpty() && !DriverManager.getCurrentPlatform().isEmpty()) {
            setTestCaseId(getTestCaseIdMap().get(DriverManager.getCurrentPlatform()));
        }
        if (PropertyResolver.isJIRASyncEnabled()) {
            TestRunManager.jiraFeedback(Collections.singletonList(this));
        }
        if (PropertyResolver.isQCSyncEnabled()) {
            TestRunManager.qcFeedback(Collections.singletonList(this));
        }

        SystemLogger.trace("Generate Allure Result: " + getName());
        //generate allure results files for this test case
        getReportBuilder().generateAllureResults(Collections.singletonList(this));
        getReportBuilder().generateEnvironmentProperties();
        if (PropertyResolver.isAllureReportServiceEnabled()) {
            TestRunManager.uploadSingleTestRunReport();
        }
        boolean previewSetting = PropertyResolver.isKeepBrowser();
        boolean isKeepBrowser = previewSetting && testRunResult.getStatus().equals(TestStatus.FAIL);
        //restart driver
        if (PropertyResolver.isRestartDriverAfterExecution() || isKeepBrowser) {
            PropertyResolver.setKeepBrowser(isKeepBrowser);
            DriverManager.closeDriver();
            PropertyResolver.setKeepBrowser(previewSetting);//reset to preview
        }
    }

    /**
     * before test
     */
    public void beforeTest() {
        //check @NonHeadless Annotation to reset driver option
        boolean shouldRestoreSession = checkNonHeadlessMethod();
        try {
            DriverManager.setCurrentPlatform("");
            TestRunManager.loadDriver(getTestCase(), getName());
        } catch (Throwable throwable) {
            SystemLogger.fatal(throwable);
        }
        if (shouldRestoreSession) {
            TestRunManager.restoreSessions();
        }
        testRunResult.setName(getName());
        testRunResult.startNow("Test Case Begin: " + getName());
        SystemLogger.info("Start Test Case: " + getName());
        invokeWithAnnotation(BeforeTest.class);
        getReportBuilder().startRecordingTest(testRunResult);
        if (testCase.getType().contains("web_app")) {
            String url = testCase.getStartURL();
            if (!url.isEmpty()) {
                DriverManager.openUrl(url);
            } else if (System.getProperty("startURL") != null) {
                DriverManager.openUrl(System.getProperty("startURL"));
            }
        }
    }

    private ReportBuilder getReportBuilder() {
        if (Objects.isNull(reportBuilder)) {
            reportBuilder = new ReportBuilder();
        }
        return reportBuilder;
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
                throw new ApollonBaseException(ApollonErrorKeys.METHOD_WITH_ANNOTATION_SHOULD_ONLY_BE_DEFINED_ONCE, annotation);
            } else {
                try {
                    methods.get(0).invoke(TestRunManager.getPerformer());
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new ApollonBaseException(ApollonErrorKeys.EXCEPTION_BY_INVOKE_ANNOTATION, ex, annotation);
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
     * @throws NoSuchFieldException   field exception
     * @throws IllegalAccessException access exception
     */
    private void updatePageObjectClass(Class<?> clazz, String config, TestType testType) throws NoSuchFieldException, IllegalAccessException {
        if (!config.isEmpty()) {
            String configLocation = PropertyResolver.getPageConfigLocation();
            JSONPageConfig jsonPageConfig = JSONContainerFactory.readTestObjectConfig(configLocation + config);
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
                    if (testType.equals(TestType.WEB_APP)) {
                        AnnotationUtils.changeFindByValue(field, value);
                    }
                }
            }
        }
    }

    private boolean checkNonHeadlessMethod() {
        boolean lastState = PropertyResolver.hasNonHeadlessMethod();
        //pre-set NonHeadLess not exist.
        PropertyResolver.setNonHeadlessMethodExists("false");
        if (PropertyResolver.useHeadlessMode()) {
            boolean useNonHeadless = steps.stream().anyMatch(TestCaseStep::hasNonHeadlessAnnotation);
            if (useNonHeadless) {
                PropertyResolver.setNonHeadlessMethodExists("true");
                if (!PropertyResolver.isRestartDriverAfterExecution()) {
                    //force driver restart, save and load session if necessary
                    TestRunManager.storeSessions();
                    DriverManager.closeDriver();
                    return true;//restore session
                }
            } else if (lastState && !PropertyResolver.isRestartDriverAfterExecution()) {
                //force driver restart, save and load session if necessary
                TestRunManager.storeSessions();
                DriverManager.closeDriver();
                return true;//restore session
            }
        }
        return false;//no restore session
    }

    public void setName(String name) {
        this.name = name;
    }

    public String prepareAndGetDisplayName() {
        TestStepMonitor.setCurrentTest(this);
        testRunResult.setThreadName(Thread.currentThread().getName());
        return name;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getOriginalName() {
        return originalName;
    }

}
