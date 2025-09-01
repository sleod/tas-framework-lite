
package io.github.sleod.tas.core.component;

import io.github.sleod.tas.common.IOUtils.FileOperation;
import io.github.sleod.tas.common.enumerations.TestStatus;
import io.github.sleod.tas.common.enumerations.TestType;
import io.github.sleod.tas.common.utils.AnnotationReflector;
import io.github.sleod.tas.common.utils.AnnotationUtils;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.core.annotations.AfterTest;
import io.github.sleod.tas.core.annotations.BeforeTest;
import io.github.sleod.tas.core.annotations.TestObject;
import io.github.sleod.tas.core.json.container.JSONPageConfig;
import io.github.sleod.tas.core.json.container.JSONTestCase;
import io.github.sleod.tas.core.json.container.JSONTestCaseStep;
import io.github.sleod.tas.core.json.container.JsonTestCaseMetaData;
import io.github.sleod.tas.core.json.deserialization.JSONContainerFactory;
import io.github.sleod.tas.core.report.ReportBuilder;
import io.github.sleod.tas.core.report.allure.ReportBuilderAllureService;
import io.github.sleod.tas.core.service.FeedbackService;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.DynamicTest;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import static io.github.sleod.tas.common.logging.SystemLogger.*;
import static io.github.sleod.tas.common.utils.StringTextUtils.isValid;

/**
 * Test Case Object to manage test case execution
 */
@Getter
@Setter
public class TestCaseObject implements Comparable<TestCaseObject> {

    //all target test objects class but not initialized
    private final Map<String, Class<?>> pageObjects = new LinkedHashMap<>();
    //all test steps of test case
    private final List<TestCaseStep> steps = new LinkedList<>();
    //test data container for the test case
    private final TestDataContainer testDataContainer;
    private final JSONTestCase testCase;
    private final TestRunResult testRunResult = new TestRunResult();
    @Setter private String testCaseId;
    @Setter private Map<String, String> testCaseIdMap;
    @Setter private String seriesNumber;
    private final String description;
    @Setter private String name;
    @Setter private String filePath;
    private final TestType testType;
    @Setter private String suiteName;
    @Setter private ReportBuilder reportBuilder;
    @Setter private JsonTestCaseMetaData jsonTestCaseMetaData;
    private final String originalName;

    /**
     * create test case object with json test case object
     *
     * @param jsonTestCase deserialize json test case
     */
    public TestCaseObject(JSONTestCase jsonTestCase) {
        this.name = jsonTestCase.getName();
        this.originalName = jsonTestCase.getName();
        testCase = jsonTestCase;
        initJsonTestCaseMetaData(testCase);
        this.testType = TestType.valueOf(jsonTestCase.getType().toUpperCase());
        this.description = testCase.getDescription();
        initTestObjects(testCase.getTestObjectNames());
        testDataContainer = new TestDataContainer(testCase.getTestDataRef(), testCase.getAdditionalTestDataFile());
        initTestSteps(testCase.getSteps());
        testRunResult.setDescription(description);
        setTestCaseId(testCase.getTestCaseId());
        setTestCaseIdMap(testCase.getTestCaseIdMap());
        if (isValid(testCase.getSeriesNumber())) {
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
        initJsonTestCaseMetaData(testCase);
        this.testType = TestType.valueOf(jsonTestCase.getType().toUpperCase());
        this.description = testCase.getDescription();
        initTestObjects(testCase.getTestObjectNames());
        testDataContainer = new TestDataContainer(testDataContent, testCase.getAdditionalTestDataFile());
        //parameterized Tests
        testRunResult.setParameters(testDataContent.getFirst());
        initTestSteps(testCase.getSteps());
        testRunResult.setDescription(description);
        setTestCaseId(testCase.getTestCaseId());
        setTestCaseIdMap(testCase.getTestCaseIdMap());
        if (isValid(testDataContent.getFirst().get("seriesNumber"))) {
            seriesNumber = testDataContent.getFirst().get("seriesNumber").toString();
        }
    }

    /**
     * init all test object with annotated name
     *
     * @param pageObjectNames annotation name
     * @throws ExceptionBase case no test objects found
     */
    public void initTestObjects(List<String> pageObjectNames) {
        String taPackageName = PropertyResolver.getTestautomationPackage();
        Set<Class<?>> annotateClazz = AnnotationReflector.getAnnotatedClass(taPackageName, TestObject.class);
        if (annotateClazz.isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.IMPLEMENTATION_NOT_FOUND, pageObjectNames, taPackageName);
        }
        for (Class<?> clazz : annotateClazz) {
            TestObject annotation = clazz.getAnnotation(TestObject.class);
            if (annotation.name().isEmpty()) {//to avoid class inherit with annotation but not specified in own class
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE,
                        clazz + " has inherited Annotation but not specified with own name. And this class will not be initialized!!");
            }
            String pageObjectName = annotation.name();
            if (pageObjectNames.contains(pageObjectName)) {
                String config = annotation.definedBy();
                //override findBy content via config file
                try {
                    updatePageObjectClass(clazz, config, testType);
                } catch (NoSuchFieldException | IllegalAccessException ex) {
                    throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Test Case Object initialization failed by update Annotation: " + pageObjectName);
                }
                pageObjects.put(pageObjectName, clazz);
            }
        }
        if (pageObjects.isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.IMPLEMENTATION_NOT_FOUND, pageObjectNames, taPackageName);
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
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, jStep.getTestObject() + "was not found!");
            }
            TestCaseStep testCaseStep = new TestCaseStep(i + 1, jsonTestCaseMetaData,
                    jStep, pageObjects.get(jStep.getTestObject()), testDataContainer);
            steps.add(testCaseStep);
        }
    }

    /**
     * Main Method to run the test
     */
    public Stream<DynamicTest> getTestSteps() {
        int retryStepOrder = -1;
        boolean testCaseToRetry = false;
        if (PropertyResolver.isRetryOnErrorEnabled()) {// prepare retry process
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

    public String getPackageName() {
        return filePath.replace("/" + FileOperation.getFileName(filePath), "");
    }

    public String getTestCaseId() {
        return Objects.isNull(testCaseId) ? "" : testCaseId;
    }

    /**
     * Only for mobile_app test case
     *
     * @return map of testcase id with key of plattform {"iOS":"id1", "android":"id2"}
     */
    public Map<String, String> getTestCaseIdMap() {
        return Objects.isNull(testCaseIdMap) ? Collections.emptyMap() : testCaseIdMap;
    }

    @Override
    public int compareTo(TestCaseObject tco) {
        int result = 0;
        String keyIn = tco.getSeriesNumber();
        String keyThis = this.getSeriesNumber();
        if (isValid(keyThis) && isValid(keyIn)) {
            result = keyThis.hashCode() - keyIn.hashCode();
        }
        return result;
    }

    /**
     * after test
     */
    public void afterTest() {
        info("Finish Test Case: " + getName());
        info("Invoke: @AfterTest");
        invokeWithAnnotation(AfterTest.class);
        testRunResult.stopNow("Test Case Ends: " + getName());
        //default after test: build Report
        getReportBuilder().stopRecordingTest(testRunResult);
        testDataContainer.clearPageObjects();
        if (getTestCaseId().isEmpty() && !getTestCaseIdMap().isEmpty() && isValid(DriverManager.getCurrentPlatform())) {
            setTestCaseId(getTestCaseIdMap().get(DriverManager.getCurrentPlatform()));
        }
        try {
            if (PropertyResolver.isJIRASyncEnabled()) {
                new FeedbackService().jiraFeedback(Collections.singletonList(this));
            }
            if (PropertyResolver.isSyncToQCEnabled()) {
                new FeedbackService().qcFeedback(Collections.singletonList(this));
            }
            if (PropertyResolver.isGenerateVideoEnabled() && !PropertyResolver.isExecutionRemoteParallelEnabled()) {
                DriverManager.stopRecordingScreen(testRunResult);
            }
        } catch (Throwable throwable) {
            info("Feedback failed: "+ throwable.getMessage());
            debug("Feedback failed: " + throwable.getMessage() + System.lineSeparator() + Arrays.toString(throwable.getStackTrace()));
        }
        info("Generate Allure Result: " + getName());
        //generate allure results files for this test case
        List<String> filePaths = getReportBuilder().generateAllureResults(this);
        try {
            if (PropertyResolver.isAllureReportServiceEnabled()) {
                info("Upload Allure Results to Server.");
                new ReportBuilderAllureService().uploadAllureResults(filePaths);
            }
        } catch (Throwable throwable) {
            debug("Allure Report Upload failed: " + throwable.getMessage() + System.lineSeparator() + Arrays.toString(throwable.getStackTrace()));
        }
        boolean previewSetting = PropertyResolver.isKeepBrowserOnErrorEnabled();
        boolean isKeepBrowser = previewSetting && testRunResult.getStatus().equals(TestStatus.FAIL);
        //restart driver
        if (PropertyResolver.isRestartDriverAfterExecutionEnabled() || isKeepBrowser) {
            PropertyResolver.setKeepBrowserOnErrorEnabled(isKeepBrowser);
            DriverManager.closeDriver();
            PropertyResolver.setKeepBrowserOnErrorEnabled(previewSetting);//reset to preview
        }
        //remove chrome profile
        FileOperation.deleteFolder(new File(PropertyResolver.getBrowserProfileDir()));
    }

    /**
     * before test
     */
    public void beforeTest() {
        //check @NonHeadless Annotation to reset driver option
        boolean shouldRestoreSession = checkNonHeadlessMethod();
        try {
            TestRunManager.loadDriver(getTestCase(), getName());
        } catch (Throwable throwable) {
            fatal(throwable);
        }
        if (shouldRestoreSession) {
            TestRunManager.restoreSessions();
        }
        testRunResult.setName(getName());
        testRunResult.startNow("Test Case Begin: " + getName());
        info("Invoke: @BeforeTest");
        invokeWithAnnotation(BeforeTest.class);
        info("Start Test Case: " + getName());
        getReportBuilder().startRecordingTest(testRunResult);
        if (testCase.getType().contains("web_app")) {
            String url = testCase.getStartURL();
            if (isValid(url)) {
                DriverManager.openUrl(url);
            } else if (isValid(PropertyResolver.getStartUrl())) {
                DriverManager.openUrl(PropertyResolver.getStartUrl());
            }
        }
        if (PropertyResolver.isGenerateVideoEnabled() && !PropertyResolver.isExecutionRemoteParallelEnabled()) {
            DriverManager.startRecordingScreen();
        }
    }

    /**
     * prepare and get display name
     * @return name to display
     */
    public String prepareAndGetDisplayName() {
        TestStepMonitor.setCurrentTest(this);
        testRunResult.setThreadName(Thread.currentThread().getName());
        return name;
    }

    /**
     * Check if there are conditions defined for the test case.
     * @return true if conditions are present, false otherwise
     */
    public boolean hasCondition() {
        return Objects.nonNull(getTestCase().getConditions());
    }

    /**
     * Check if random line usage is enabled.
     * @return true if random line usage is enabled, false otherwise
     */
    public boolean isUseRandomLine() {
        return getTestCase().getConditions().isUseRandomLine();
    }

    /**
     * Get the variant index for the test case.
     * @return the variant index
     */
    public List<Integer> getVariantIndex() {
        return getTestCase().getConditions().getIndex();
    }

    /**
     * Get the variant limit for the test case.
     * @return the variant limit
     */
    public int getVariantLimit() {
        return getTestCase().getConditions().getLimit();
    }

    /**
     * Get the report builder instance.
     * @return the report builder
     */
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
                throw new ExceptionBase(ExceptionErrorKeys.METHOD_WITH_ANNOTATION_SHOULD_ONLY_BE_DEFINED_ONCE, annotation);
            } else {
                try {
                    methods.get(0).invoke(TestRunManager.getPerformer());
                } catch (IllegalAccessException | InvocationTargetException ex) {
                    throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_INVOKE_ANNOTATION, ex, annotation);
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

    /**
     * Check if there is a non-headless method present.
     * @return true if a non-headless method is present, false otherwise
     */
    private boolean checkNonHeadlessMethod() {
        boolean lastState = PropertyResolver.hasNonHeadlessMethod();
        //pre-set NonHeadLess not exist.
        PropertyResolver.setNonHeadlessMethodExists("false");
        if (PropertyResolver.isHeadlessModeEnabled()) {
            boolean useNonHeadless = steps.stream().anyMatch(TestCaseStep::hasNonHeadlessAnnotation);
            if (useNonHeadless) {
                PropertyResolver.setNonHeadlessMethodExists("true");
                if (!PropertyResolver.isRestartDriverAfterExecutionEnabled()) {
                    //force driver restart, save and load session if necessary
                    TestRunManager.storeSessions();
                    DriverManager.closeDriver();
                    return true;//restore session
                }
            } else if (lastState && !PropertyResolver.isRestartDriverAfterExecutionEnabled()) {
                //force driver restart, save and load session if necessary
                TestRunManager.storeSessions();
                DriverManager.closeDriver();
                return true;//restore session
            }
        }
        return false;//no restore session
    }

    /**
     * Initialize JSON test case metadata.
     * @param testCase
     */
    private void initJsonTestCaseMetaData(JSONTestCase testCase) {
        jsonTestCaseMetaData = new JsonTestCaseMetaData();
        jsonTestCaseMetaData.setScreenshotLevel(testCase.getScreenshotLevel());
        jsonTestCaseMetaData.setTestType(TestType.valueOf(testCase.getType().toUpperCase()));
    }
}
