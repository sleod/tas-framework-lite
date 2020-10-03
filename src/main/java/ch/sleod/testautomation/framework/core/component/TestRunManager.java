package ch.sleod.testautomation.framework.core.component;

import ch.sleod.testautomation.framework.common.IOUtils.FileLocator;
import ch.sleod.testautomation.framework.common.IOUtils.FileOperation;
import ch.sleod.testautomation.framework.common.enumerations.TestStatus;
import ch.sleod.testautomation.framework.common.enumerations.TestType;
import ch.sleod.testautomation.framework.common.logging.ScreenCapture;
import ch.sleod.testautomation.framework.common.utils.TimeUtils;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.json.container.JSONRunnerConfig;
import ch.sleod.testautomation.framework.core.json.container.JSONTestCase;
import ch.sleod.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.sleod.testautomation.framework.core.report.ReportBuilder;
import ch.sleod.testautomation.framework.rest.TFS.connection.QUERY_OPTION;
import ch.sleod.testautomation.framework.rest.TFS.connection.TFSRestClient;
import com.codeborne.selenide.Configuration;
import com.google.common.collect.Multimap;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.*;
import static ch.sleod.testautomation.framework.common.utils.ObjectWriterReader.*;
import static java.util.Arrays.asList;

public class TestRunManager {

    private static PerformableTestCases performer;
    private static String currentISOAppName;
    private static String currentAppName;
    private static String currentAppActivity;
    private static JSONRunnerConfig runnerConfig = null;

    public static PerformableTestCases getPerformer() {
        return performer;
    }

    public static void setPerformer(PerformableTestCases performer) {
        TestRunManager.performer = performer;
    }

    public static String getCurrentAppName() {
        return currentAppName;
    }

    public static String getCurrentAppActivity() {
        return currentAppActivity;
    }

    public static String getCurrentISOAppName() {
        return currentISOAppName;
    }

    /**
     * filter test case with meta tags
     *
     * @param metaFilters  defined meta tags
     * @param jsonTestCase test case
     * @return if to be added
     */
    public static boolean filterTestCase(List<String> metaFilters, JSONTestCase jsonTestCase) {
        if (metaFilters == null || metaFilters.isEmpty()) {
            return true;
        }
        List<String> testCaseMetas = jsonTestCase.getMeta();
        boolean toBeAdd = false;
        for (String metaTag : testCaseMetas) {
            String exclude = metaTag.replace("@", "-");
            String include = metaTag.replace("@", "+");
            if (metaFilters.contains(exclude)) {
                toBeAdd = false;
                break;
            }
            if (metaFilters.contains(include)) {
                toBeAdd = true;
            }
        }
        return toBeAdd;
    }

    /**
     * find files of test cases
     *
     * @param fileBaseDir         file directory
     * @param includeFilePatterns include patterns
     * @param excludeFilePatterns exclude patterns
     * @return file paths of test cases
     */
    public static List<String> filePaths(String fileBaseDir, List<String> includeFilePatterns, List<String> excludeFilePatterns) {
        List<String> paths = FileLocator.findPaths(FileLocator.findResource(fileBaseDir),
                includeFilePatterns,
                excludeFilePatterns,
                fileBaseDir);
        log("TRACE", "Paths found: " + Arrays.toString(paths.toArray()));
        return paths;
    }

    /**
     * init test cases with file paths of json filtered with meta notation
     *
     * @param filePaths   file paths
     * @param metaFilters meta filters
     */
    public static void initTestCases(List<String> filePaths, List<String> metaFilters) throws IOException {
        Multimap<String, String> coverage = null;//coverage with test case ids in multimap
        List<String> selectedIds = Collections.emptyList();
        if (PropertyResolver.isTFSFeedbackEnabled()) {//check for feedback to tfs
            coverage = runnerConfig.getCoverageMap();
            if (runnerConfig.isFullRun()) {//check run config for full run
                selectedIds = getFullRunTestCaseIds(runnerConfig);
            } else if (runnerConfig.isFailureRetest()) {//check run config for retest run
                selectedIds = getLastFailedTestCaseIds(runnerConfig);
            } else {//check run config for selected ids to run
                selectedIds = asList(runnerConfig.getSelectedTestCaseIds());
            }
        }
        if (filePaths.isEmpty()) {
            warn("There is no json test case in defined folder! ->" + PropertyResolver.getDefaultTestCaseLocation());
            System.exit(-1);
        }
        if (!metaFilters.isEmpty()) {
            trace("Filters: " + Arrays.toString(metaFilters.toArray()));
        } else {
            trace("No Filter is set, all found test case will be executed!");
        }
        if (metaFilters.contains("")) {
            warn("meta filters contains empty value!");
        }
        List<TestCaseObject> testCaseObjects = new LinkedList<>();
        for (String filePath : filePaths) {
            JSONTestCase jsonTestCase = JSONContainerFactory.buildJSONTestCaseObject(filePath);
            //filter with meta tags
            if (filterTestCase(metaFilters, jsonTestCase) && isSelected(jsonTestCase, coverage, selectedIds)) {
                testCaseObjects.addAll(normalizeRepeatTestCases(new TestCaseObject(jsonTestCase)));
            }
        }
        getPerformer().setTestCaseObjects(testCaseObjects);
    }

    /**
     * Get failed test cases in last run with plan id and suite id in config
     * **** CHANGE: all test cases except success cases will be added to run (24.04.2020)
     *
     * @param runnerConfig run config
     * @return list of test case ids
     */
    private static List<String> getLastFailedTestCaseIds(JSONRunnerConfig runnerConfig) {
        String suiteId = runnerConfig.getSuiteId();
        String planId = runnerConfig.getPlanId();
        TFSRestClient restClient = new TFSRestClient(runnerConfig.getTfsConfig());
//        return restClient.getTestCaseIdsInPlan(planId, suiteId, QUERY_OPTION.FAILED_ONLY);
        return restClient.getTestCaseIdsInPlan(planId, suiteId, QUERY_OPTION.EXCEPT_SUCCESS);
    }

    /**
     * Get all test cases in last run with plan id and suite id in config
     *
     * @param runnerConfig run config
     * @return list of test case ids
     */
    private static List<String> getFullRunTestCaseIds(JSONRunnerConfig runnerConfig) {
        String suiteId = runnerConfig.getSuiteId();
        String planId = runnerConfig.getPlanId();
        TFSRestClient restClient = new TFSRestClient(runnerConfig.getTfsConfig());
        return restClient.getTestCaseIdsInPlan(planId, suiteId, QUERY_OPTION.ALL);
    }

    /**
     * load driver while init test case object
     *
     * @param jsonTestCase json test case
     */
    public static void loadDriver(JSONTestCase jsonTestCase, String testCaseName) {
        trace("Load driver for: " + testCaseName);
        //load all driver config
        TestType type = TestType.valueOf(jsonTestCase.getType().toUpperCase());
        switch (type) {
            case WEB_APP:
                //init web driver for runs
                DriverManager.setupWebDriver();
                //set screenshot taker
                ScreenCapture.setScreenTaker(DriverManager.getWebDriverProvider());
                break;
            case REST:
                DriverManager.setupRestDriver();
                break;
            case MOBILE_IOS:
                DriverManager.setupMobileAppDriver(type);
                currentISOAppName = jsonTestCase.getAppName();
                ScreenCapture.setScreenTaker(DriverManager.getMobileAppDriverProvider());
                break;
            case MOBILE_ANDROID:
                DriverManager.setupMobileAppDriver(type);
                currentAppName = jsonTestCase.getAppName();
                currentAppActivity = jsonTestCase.getActivity();
                ScreenCapture.setScreenTaker(DriverManager.getMobileAppDriverProvider());
                break;
            case MOBILE_WEB_APP:
                DriverManager.setupRemoteWebDriver();
                ScreenCapture.setScreenTaker(DriverManager.getRemoteWebDriverProvider());
                break;
            case APP:
                DriverManager.setupNonDriver();
        }
    }

    /**
     * check if given test case is selected to run and update the coverage with selection
     *
     * @param jsonTestCase test case json context
     * @param coverage     list of covered test case ids
     * @param selectedIds  list if selected id
     * @return if current test case selected
     */
    private static boolean isSelected(JSONTestCase jsonTestCase, Multimap<String, String> coverage, List<String> selectedIds) {
        if (!PropertyResolver.isTFSFeedbackEnabled()) {
            return true;
        } else if (selectedIds == null || selectedIds.isEmpty()) {
            return false;
        }
        boolean isSelected = false;
        String fileName = jsonTestCase.getFileName();
        if (coverage != null && coverage.containsKey(fileName)) {
            List<String> ids = new LinkedList<>(coverage.asMap().get(fileName));
            List<String> selected = new LinkedList<>();
            for (String id : ids) {
                if (selectedIds.contains(id)) {
                    isSelected = true;
                    selected.add(id);
                }
            }
            jsonTestCase.setCoverage(selected);
        }
        return isSelected;
    }

    /**
     * Normalize test case with multiple test data, for example: test data in csv file
     *
     * @param testCaseObject test case object
     * @return list of test case objects
     */
    private static List<TestCaseObject> normalizeRepeatTestCases(TestCaseObject testCaseObject) {
        if (testCaseObject.getTestDataContainer().isRepeat()) {
            ArrayList<TestCaseObject> normCases = new ArrayList<>(testCaseObject.getTestDataContainer().getDataContentSize());
            int index = 1;
            //for each row of test data, create new test case object
            for (Map<String, Object> testData : testCaseObject.getTestDataContainer().getDataContent()) {
                TestCaseObject tco = new TestCaseObject(testCaseObject.getTestCase(), Collections.singletonList(testData), " Variante " + index++);
                if (PropertyResolver.isTFSFeedbackEnabled() && !testData.containsKey("testCaseId")) {
                    warn("To return result back to TFS the test case id is required in csv or db data. " +
                            "Please set test case id with column name 'testCaseId' for every test data line!");
                } else {
                    if (PropertyResolver.isTFSFeedbackEnabled()) {
                        String testCaseId = testData.get("testCaseId").toString();
                        if (testCaseObject.getTestCase().getCoverage().contains(testCaseId)) {
                            tco.setTestCaseId(testCaseId);
                            addNormCase(normCases, tco);
                        } else {
                            trace("TFS test case id in test data is not selected and this test case will be ignored!");
                        }
                    } else {
                        addNormCase(normCases, tco);
                    }
                }
            }
            return normCases;
        } else {
            if (testCaseObject.getTestCaseId() == null && !testCaseObject.getTestCase().getCoverage().isEmpty()) {
                testCaseObject.setTestCaseId(testCaseObject.getTestCase().getCoverage().get(0));
            }
            return addNormCase(new ArrayList<>(1), testCaseObject);
        }
    }

    /**
     * handling series number and build sequenced test case runner
     *
     * @param normCases list of single cases
     * @param tco       test case object
     * @return list of single test cases
     */
    private static ArrayList<TestCaseObject> addNormCase(ArrayList<TestCaseObject> normCases, TestCaseObject tco) {
        if (tco.getSeriesNumber() != null && !tco.getSeriesNumber().isEmpty()) {
            addSequencedCase(tco);
        }
        normCases.add(tco);
        return normCases;
    }

    private static void addSequencedCase(TestCaseObject tco) {
        String seriesNumber = tco.getSeriesNumber();
        int index = seriesNumber.lastIndexOf(".");
        if (index > 0) {
            getPerformer().addSequenceCaseRunner(seriesNumber.substring(0, index), tco);
        } else {
            throw new RuntimeException("Series Number has wrong format!! Please Check the form of Series Number for: "
                    + seriesNumber + "\n" + "Format of SeriesNumber: <context1>.<context2>...<number>");
        }
    }

    /**
     * generate Report with runResult
     *
     * @param testCaseObjects test objects
     */
    public static void generateReport(List<TestCaseObject> testCaseObjects) {
        try {
            ReportBuilder.generateReport(testCaseObjects);
        } catch (IOException ex) {
            error(ex);
        }
    }

    /**
     * Feed test results back to TFS after run
     *
     * @param testCaseObjects test case objects after test run
     */
    public static void tfsFeedback(List<TestCaseObject> testCaseObjects) {
        //get data from test case object
        Map<String, TestRunResult> testRunResultMap = new HashMap<>(testCaseObjects.size());
        for (TestCaseObject testCaseObject : testCaseObjects) {
            String testCaseId = testCaseObject.getTestCaseId();
            if (testCaseId == null) {
                throw new RuntimeException("Test Case Id in Test Case Object is Null. Without Id no update to TFS!");
            }
            testRunResultMap.put(testCaseId, testCaseObject.getTestRunResult());
        }
        //create test run with plan id, suite id and test points via test case id, if not done
        TFSRestClient restClient = new TFSRestClient(runnerConfig.getTfsConfig());
        String suiteId = runnerConfig.getSuiteId();
        String planId = runnerConfig.getPlanId();
        String runName = runnerConfig.getRunName();
        JSONObject testRun = restClient.createTestRun(runName, planId, suiteId, new ArrayList<>(testRunResultMap.keySet()));
        String tfsRunId = testRun.getString("id");
        //update test run with test run result
        JSONObject results = restClient.updateTestRunResults(tfsRunId, testRunResultMap);
        JSONArray values = results.getJSONArray("value");
        //upload log file and screenshots as attachments
        for (int i = 0; i < testCaseObjects.size(); i++) {
            TestCaseObject testCaseObject = testCaseObjects.get(i);
            String stream = FileOperation.encodeFileToBase64(new File(testCaseObject.getTestRunResult().getLogFilePath()));
            JSONObject singleResult = (JSONObject) values.get(i);
            restClient.attachmentToTestResult(tfsRunId, singleResult.getString("id"), "Report.log", "Default Report", stream);
            if (testCaseObject.getTestRunResult().getStatus().equals(TestStatus.FAIL)) {
                //upload screenshot
                testCaseObject.getTestRunResult().getAttachments().forEach(attachmentFile -> {
                    String fileStream = FileOperation.encodeFileToBase64(attachmentFile);
                    restClient.attachmentToTestResult(tfsRunId, singleResult.getString("id"), attachmentFile.getName(), "Screenshots", fileStream);
                });
            }
        }
        //create a tfs test run with test points
        restClient.setTestRunState(tfsRunId, "completed", TimeUtils.getISOTimestamp());
    }

    /**
     * copy resources files in defined locations to current local folder.
     */
    public static void retrieveResources(String propertyFilePath) {
        boolean isDefaultPath = PropertyResolver.loadTestRunProperties(propertyFilePath);
        try {
            if (isDefaultPath) {
                URL folderInJar = TestRunManager.class.getClassLoader().getResource("properties");
                if (folderInJar != null && folderInJar.toURI().getScheme().equals("jar")) {
                    File jarFile = new File(folderInJar.getFile()).getParentFile();
                    String baseDir = FileLocator.getProjectBaseDir() + "/src/main/resources/";
                    String jarPath = "jar:file:" + jarFile.getAbsolutePath().split("file:")[1].replace("\\", "/") + "/";
                    trace("Jar Path: " + jarPath);
                    URI jarUri = URI.create(jarPath);
                    //retrieve property file and rename to normal
                    File propertyFile = new File(baseDir + "properties/TestRunProperties.properties");
                    FileOperation.retrieveFileFromResources("properties/DefaultTestRunProperties.properties", propertyFile);
                    //build file system and find folders
//                    FileSystem fileSystem = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
                    for (String folder : PropertyResolver.getAllPropertiesWith("location")) {
                        trace("Property listed folder: " + folder);
                        new File(baseDir + folder).mkdir();
//                        if (folder.endsWith("/")) {
//                            folder = folder.substring(0, folder.length() - 1);
//                        }
//                        Path path = fileSystem.getPath(folder);
//                        //list folder recursive
//                        List<Path> filePaths = FileLocator.walkThrough(path);
//                        //retrieve file to local
//                        for (Path filePath : filePaths) {
//                            trace("Try to retrieve file: " + path);
//                            FileOperation.retrieveFileFromResources(filePath.toString(), new File(baseDir + filePath));
//                        }
                    }
                }
            }
            if (PropertyResolver.isTFSFeedbackEnabled()) {
                String configName = PropertyResolver.getTFSRunnerConfigFile();
                runnerConfig = JSONContainerFactory.getRunnerConfig(configName);
                //init test plan configuration with given id
                String configId = "";
                if (PropertyResolver.getTFSConfigurationID() != null && !PropertyResolver.getTFSConfigurationID().isEmpty()) {
                    configId = PropertyResolver.getTFSConfigurationID();
                } else if (runnerConfig.getConfigurationId() != null && !runnerConfig.getConfigurationId().isEmpty()) {
                    configId = runnerConfig.getConfigurationId();
                }
                if (!configId.isEmpty()) {
                    TFSRestClient restClient = new TFSRestClient(runnerConfig.getTfsConfig());
                    runnerConfig.setTestPlanConfig(restClient.getTestPlanConfiguration(configId));
                    //set test plan configuration to system property
                    PropertyResolver.setProperties(runnerConfig.getTestPlanConfig());
                    if (!PropertyResolver.getTFSRunnerConfigFile().equals(configName)) {
                        runnerConfig = JSONContainerFactory.getRunnerConfig(PropertyResolver.getTFSRunnerConfigFile());
                    }
                } else warn("No Test Plan Configuration set, Test Run with local tfs runner config!");
            }
        } catch (URISyntaxException | IOException ex) {
            error(ex);
        }
    }

    /**
     * Store Test results to DB
     *
     * @param testCaseObjects list of test case objects
     */
    public static void storeResultsToDB(List<TestCaseObject> testCaseObjects) {
        //todo: store results to DB
    }

    /**
     * Restore Cookies and URL of Web Driver
     *
     * @param webDriver Web Driver
     */
    @SuppressWarnings("unchecked")
    public static void restoreSessions(WebDriver webDriver) {
        if (new File("target/logs/cookies.txt").exists()) {
            ArrayList<Cookie> cookies = (ArrayList<Cookie>) readObject(new ArrayList<Cookie>(), "target/logs/cookies.txt");
            String currentURL = (String) readObject("", "target/logs/currentURL.txt");
            if (cookies != null) {
                cookies.forEach(cookie -> webDriver.manage().addCookie(cookie));
            }
            if (currentURL != null && !currentURL.isEmpty()) {
                webDriver.navigate().to(currentURL);
            }
        }
    }

    /**
     * Store Cookies and URL of Web Driver
     *
     * @param webDriver Web Driver
     */
    public static void storeSessions(WebDriver webDriver) {
        WriteObject(new ArrayList<>(webDriver.manage().getCookies()), "target/logs/cookies.txt");
        WriteObject(webDriver.getCurrentUrl(), "target/logs/currentURL.txt");
    }

    /**
     * store failed step for next retry
     *
     * @param testCaseFolder test case object
     * @param testCaseName   test case object
     * @param stepOrder      oder of step
     */
    public static void storeRetryStep(String testCaseFolder, String testCaseName, int stepOrder) {
        String retryTestCaseID = testCaseFolder + "." + testCaseName;
        WriteObject(retryTestCaseID, "target/logs/retryTestCaseID.txt");
        WriteObject(stepOrder, "target/logs/stepOrder.txt");
    }

    /**
     * load logged test step order number for retry
     *
     * @return number of step order
     */
    public static int loadRetryStepOrder() {
        if (new File("target/logs/stepOrder.txt").exists()) {
            return (Integer) readObject("", "target/logs/stepOrder.txt");
        } else {
            return -1;
        }
    }

    /**
     * load logged test case id for retry
     *
     * @return test case id
     */
    public static String loadRetryTestCaseID() {
        if (new File("target/logs/retryTestCaseID.txt").exists()) {
            return (String) readObject("", "target/logs/retryTestCaseID.txt");
        } else {
            return "";
        }
    }

    /**
     * the file named with "xxxx-test-data-global.json" will be loaded automatically
     */
    public static void loadGlobalTestData() {
        try {
            List<Path> paths = FileLocator.listRegularFilesRecursiveMatchedToName(
                    Objects.requireNonNull(FileLocator.findResource(PropertyResolver.getDefaultTestDataLocation())).toString(), 5, "testdata-global");
            if (paths.size() > 1) {
                throw new RuntimeException("Test Data File with name 'xxx-testdata-global.json' should be single in testData folder!");
            } else if (paths.size() == 1) {
                TestDataContainer.loadGlobalTestData(paths.get(0));
            }
        } catch (IOException e) {
            error(e);
        }
    }

    public static boolean feedbackAfterSingleTest() {
        return runnerConfig.feedbackAfterSingleTest();
    }

}
