package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.PropertyKey;
import ch.qa.testautomation.framework.common.enumerations.TestType;
import ch.qa.testautomation.framework.common.logging.ScreenCapture;
import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.container.JSONRunnerConfig;
import ch.qa.testautomation.framework.core.json.container.JSONTestCase;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.core.report.ReportBuilder;
import ch.qa.testautomation.framework.core.report.allure.ReportBuilderAllureService;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import ch.qa.testautomation.framework.rest.base.QUERY_OPTION;
import ch.qa.testautomation.framework.rest.hpqc.connection.QCRestClient;
import ch.qa.testautomation.framework.rest.jira.connection.JIRARestClient;
import com.beust.jcommander.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.*;
import static ch.qa.testautomation.framework.common.utils.ObjectWriterReader.WriteObject;
import static ch.qa.testautomation.framework.common.utils.ObjectWriterReader.readObject;
import static ch.qa.testautomation.framework.common.utils.StringTextUtils.isValid;
import static ch.qa.testautomation.framework.configuration.PropertyResolver.getTestCaseLocation;
import static ch.qa.testautomation.framework.core.json.ObjectMapperSingleton.getObjectMapper;
import static java.util.Arrays.asList;

public class TestRunManager {

    private static JSONRunnerConfig tfsRunnerConfig = null;
    private static JSONRunnerConfig jiraExecutionConfig = null;
    private static final Map<String, PerformableTestCases> performerMap = new HashMap<>();

    public static PerformableTestCases getPerformer() {
        return performerMap.get(Thread.currentThread().getName());
    }

    public static void setPerformer(PerformableTestCases performer) {
        performerMap.put(Thread.currentThread().getName(), performer);
    }

    /**
     * allow adding extra plain text attachment with limited extensions for single test case
     * allowed extensions: "txt", "csv", "log", "out", "pdf", "png", "jpg", "mp4"
     *
     * @param filePath file path
     */
    public static void addExtraAttachment4TestCase(String filePath) {
        if (isValid(filePath)) {
            File attachment = new File(filePath);
            if (attachment.exists() && attachment.isFile() && FileOperation.isAllowedFileExtension(filePath)) {
                ReportBuilder.addExtraAttachment4TestCase(attachment);
            } else {
                warn("Given File can not be attach to Test Case!" + filePath +
                        "\nFile Path must be correct with allowed extensions: " + FileOperation.getAllowedFileExtension());
            }
        }
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

        //get meta info in test case
        List<String> testCaseMetas = jsonTestCase.getMeta();
        boolean toBeAdd = false;
        //replace
        for (String metaTag : testCaseMetas) {
            String exclude1 = metaTag.replace("@", "-");
            String exclude2 = metaTag.replace("@", "-@");
            String include1 = metaTag.replace("@", "+");
            String include2 = metaTag.replace("@", "+@");
            if (metaFilters.contains(exclude1) || metaFilters.contains(exclude2)) {
                toBeAdd = false;
                break;
            }
            if (metaFilters.contains(include1) || metaFilters.contains(include2)) {
                toBeAdd = true;
                trace("Pickup Test Case: " + jsonTestCase.getName());
            }
        }
        return toBeAdd;
    }

    /**
     * find files of test cases
     *
     * @param includeFilePatterns include patterns
     * @param excludeFilePatterns exclude patterns
     * @return file paths of test cases
     */
    public static List<String> findAllFilePathOfTestCaseFile(List<String> includeFilePatterns, List<String> excludeFilePatterns) {
        List<String> paths = FileLocator.findPaths(FileLocator.findResource(getTestCaseLocation()),
                includeFilePatterns,
                excludeFilePatterns,
                getTestCaseLocation());
        log("TRACE", "Paths found: " + Arrays.toString(paths.toArray()));
        return paths;
    }

    /**
     * init test cases with file paths of json filtered with meta notation
     *
     * @param filePaths   file paths
     * @param metaFilters meta filters
     */
    public static List<TestCaseObject> initTestCases(List<String> filePaths, List<String> metaFilters) {
        List<String> selectedIds = Collections.emptyList();
        if (PropertyResolver.isJIRASyncEnabled()) {
            if (jiraExecutionConfig.isFullRun()) {//check run config for full run
                selectedIds = getJiraTestCaseIdsInExecution(jiraExecutionConfig, QUERY_OPTION.ALL);
            } else if (jiraExecutionConfig.isFailureRetest()) {//check run config for retest run
                selectedIds = getJiraTestCaseIdsInExecution(jiraExecutionConfig, QUERY_OPTION.EXCEPT_SUCCESS);
            } else {//check run config for selected ids to run
                selectedIds = asList(jiraExecutionConfig.getSelectedTestCaseIds());
            }
            if (selectedIds.isEmpty()) {
                throw new ApollonBaseException(ApollonErrorKeys.FAIL_ON_GET_TESTCASE_ID_FROM_JIRA);
            }
        }
        if (filePaths.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.TEST_CASE_NOT_FOUND, PropertyResolver.getTestCaseLocation());
        }
        if (!metaFilters.isEmpty()) {
            trace("Filters: " + Arrays.toString(metaFilters.toArray()));
        } else {
            warn("No Filter is set, all found test case will be executed!");
        }
        if (metaFilters.contains("")) {
            warn("meta filters contains empty value!");
        }
        List<TestCaseObject> testCaseObjects = new LinkedList<>();
        for (String filePath : filePaths) {
            JSONTestCase jsonTestCase = JSONContainerFactory.buildJSONTestCaseObject(filePath);
            //filter with meta tags
            if (filterTestCase(metaFilters, jsonTestCase)) {
                TestCaseObject testCaseObject = new TestCaseObject(jsonTestCase);
                testCaseObject.setFilePath(filePath);
                List<TestCaseObject> normalizedTestCases = normalizeRepeatTestCases(testCaseObject);
                for (TestCaseObject n_testCaseObject : normalizedTestCases) {
                    if (isSelected(n_testCaseObject, selectedIds)) {
                        testCaseObjects.add(n_testCaseObject);
                    }
                }
            }
        }
        if (testCaseObjects.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.TEST_CASE_NOT_FOUND, "With Meta Filter: " + Strings.join(",", metaFilters));
        }
        checkDuplicateNaming(testCaseObjects);
        return testCaseObjects;
    }

    public static void cleanResultsByPresent() {
        trace("Clean Allure Results on Server if present.");
        new ReportBuilderAllureService().cleanResultsByPresent();
    }

    public static void uploadSingleTestRunReport() {
        trace("Upload Allure Results to Server.");
        new ReportBuilderAllureService().uploadAllureResults();
    }

    public static void generateReportOnService() {
        trace("Generate Allure Report on Server.");
        new ReportBuilderAllureService().generateReportOnService();
    }

    /**
     * Checks if multiple testcases have the same name or id.
     *
     * @param testCaseObjects list of type testCaseObject.
     * @throws ApollonBaseException if duplicate found.
     */
    private static void checkDuplicateNaming(List<TestCaseObject> testCaseObjects) {
        Set<String> testCaseNames = new HashSet<>();
        Set<String> testCaseIds = new HashSet<>();

        for (TestCaseObject testCaseObject : testCaseObjects) {
            if (!testCaseNames.add(testCaseObject.getName())) {
                throw new ApollonBaseException(ApollonErrorKeys.DEFINED_MULTIPLE_FILES, testCaseObject.getName());
            }
            //If ID is set, check if duplicate in case feedback enabled
            if ((PropertyResolver.isTFSSyncEnabled() || PropertyResolver.isJIRASyncEnabled())
                    && !testCaseObject.getTestCaseId().isEmpty() && !testCaseObject.getTestCaseId().equals("-")) {
                if (!testCaseIds.add(testCaseObject.getTestCaseId())) {
                    throw new ApollonBaseException(ApollonErrorKeys.DEFINED_MULTIPLE_TESTCASES, testCaseObject.getTestCaseId());
                }
            }

        }
    }


    /**
     * Get all test cases in last run with Jira execution id in config
     *
     * @param jiraExecutionConfig run config
     * @param query_option        {@link QUERY_OPTION}
     * @return list of test case ids
     */
    private static List<String> getJiraTestCaseIdsInExecution(JSONRunnerConfig jiraExecutionConfig, QUERY_OPTION query_option) {
        return getJiraRestClient().getTestsInExecution(jiraExecutionConfig.getTestExecutionId(), query_option);
    }

    private static JIRARestClient getJiraRestClient() {
        return new JIRARestClient(PropertyResolver.getJiraHost(), PropertyResolver.getJiraPAT());
    }

    private static QCRestClient getQCRestClient() {
        return new QCRestClient(PropertyResolver.getQCConfigFile());
    }

    /**
     * load driver while init test case object
     *
     * @param jsonTestCase json test case
     * @param testCaseName test case name
     */
    public static void loadDriver(JSONTestCase jsonTestCase, String testCaseName) {
        trace("Load driver for: " + testCaseName);
        //load all driver config
        TestType type = TestType.valueOf(jsonTestCase.getType().toUpperCase());
        switch (type) {
            case WEB_APP -> {//local
                DriverManager.setupWebDriver();
                ScreenCapture.setScreenTaker(DriverManager.getWebDriverProvider());
            }
            case REST -> DriverManager.setupRestDriver();
            case APP -> DriverManager.setupNonDriver();
            default ->
                    throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "Load Driver failed with unknown type: " + type);
        }
    }

    /**
     * check if given test case is selected to run and update the coverage with selection
     *
     * @param testCaseObject test case object
     * @param selectedIds    list if selected id
     * @return if current test case selected
     */
    private static boolean isSelected(TestCaseObject testCaseObject, List<String> selectedIds) {
        if (!PropertyResolver.isTFSSyncEnabled() && !PropertyResolver.isJIRASyncEnabled()) {
            return true;
        } else if (selectedIds == null || selectedIds.isEmpty()) {
            warn("List of Selected IDs is Empty!");
            return false;
        } else {
            String testCaseId = testCaseObject.getTestCaseId();
            Map<String, String> testCaseIdMap = testCaseObject.getTestCaseIdMap();
            if (selectedIds.contains(testCaseId)) {
                return true;
            } else {
                return testCaseIdMap.values().stream().anyMatch(selectedIds::contains);
            }
        }
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
            //for each row of test data, create new test case object
            List<Map<String, Object>> dataContent = testCaseObject.getTestDataContainer().getDataContent();
            for (int index = 0; index < dataContent.size(); index++) {
                Map<String, Object> testData = dataContent.get(index);
                TestCaseObject new_tco = new TestCaseObject(testCaseObject.getTestCase(), Collections.singletonList(testData), " - Variante " + (index + 1));
                new_tco.setFilePath(testCaseObject.getFilePath());
                if (!isFiltered(testData)) {
                    continue;
                }
                if (PropertyResolver.isTFSSyncEnabled() || PropertyResolver.isJIRASyncEnabled()) {
                    if (!testData.containsKey("testCaseId") && testData.get("testCaseId") != null && !testData.get("testCaseId").toString().isEmpty()) {
                        throw new ApollonBaseException(ApollonErrorKeys.TEST_CASE_ID_IS_REQUIRED, testCaseObject.getName());
                    } else {//add tc to list with valid test case id
                        if (!new_tco.getType().equals(TestType.MOBILE_APP)) {//normal case
                            Object testCaseId = testData.get("testCaseId");
                            if (Objects.nonNull(testCaseId) && !Objects.equals(testCaseId, "-")) {//avoid execution with '-'
                                new_tco.setTestCaseId(testCaseId.toString());
                                normCases.add(new_tco);
                            } else {//no add to list
                                trace(new_tco.getName() + " is ignored because of '-' value in testCaseId.");
                            }
                        } else {//mobile App case, check test case id map
                            if (Objects.nonNull(testData.get("testCaseIdMap"))) {
                                try {
                                    Map<String, String> testCaseIdMap = getObjectMapper().readValue(testData.get("testCaseIdMap").toString(), new TypeReference<>() {
                                    });
                                    new_tco.setTestCaseIdMap(testCaseIdMap);
                                } catch (JsonProcessingException ex) {
                                    throw new ApollonBaseException(ApollonErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, testData.get("testCaseIdMap"));
                                }
                            }
                        }
                    }
                } else {//regardless test case id
                    normCases.add(new_tco);
                }
            }
            return normCases;
        } else {
            ArrayList<TestCaseObject> singleList = new ArrayList<>(1);
            if (PropertyResolver.isTFSSyncEnabled() || PropertyResolver.isJIRASyncEnabled()) {
                if (testCaseObject.getTestCaseId().isEmpty() && !testCaseObject.getType().equals(TestType.MOBILE_APP)
                        || testCaseObject.getType().equals(TestType.MOBILE_APP) && testCaseObject.getTestCaseIdMap().isEmpty()) {
                    throw new ApollonBaseException(ApollonErrorKeys.TEST_CASE_ID_IS_REQUIRED, testCaseObject.getName());
                } else if (testCaseObject.getTestCaseId().equals("-")) {
                    warn(testCaseObject.getName() + " with testcase id '-' will not be executed!");
                } else {
                    singleList.add(testCaseObject);
                }
            } else {
                singleList.add(testCaseObject);
            }
            return singleList;
        }
    }

    private static boolean isFiltered(Map<String, Object> testData) {
        Map<String, String> selection = getPerformer().getCSVTestDataSelectionFilter();
        Map<String, String> exclusion = getPerformer().getCSVTestDataExclusionFilter();
        if (exclusion.isEmpty() && selection.isEmpty()) {
            return true;
        } else if (!exclusion.isEmpty() && !selection.isEmpty()) {
            warn("Both selection and exclusion filters are filled with data. Both filters are ignored!");
            return true;
        } else {
            if (!selection.isEmpty()) {
                return containedInFilter(testData, selection);
            } else {
                return !containedInFilter(testData, exclusion);
            }
        }
    }

    private static boolean containedInFilter(Map<String, Object> testData, Map<String, String> filter) {
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (testData.get(key) != null && testData.get(key).equals(value)) {
                return true;
            }
        }
        return false;
    }

    public synchronized static void jiraFeedback(List<TestCaseObject> testCaseObjects) {
        trace("Feedback Test Result back to JIRA...");
        try {
            getJiraRestClient().updateRunStatusInExecution(jiraExecutionConfig, getTestRunResultMap(testCaseObjects));
        } catch (Throwable throwable) {
            SystemLogger.warn("JIRA Feedback failed: " + throwable.getMessage());
        }
    }

    public synchronized static void qcFeedback(List<TestCaseObject> testCaseObjects) {
        trace("Feedback Test Result back to QC...");
        try {
            getQCRestClient().syncTestCasesRunResults(testCaseObjects);
        } catch (Throwable throwable) {
            SystemLogger.warn("QC Feedback failed: " + throwable.getMessage());
        }
    }

    public static Map<String, TestRunResult> getTestRunResultMap(List<TestCaseObject> testCaseObjects) {
        Map<String, TestRunResult> testRunResultMap = new HashMap<>(testCaseObjects.size());
        //get data from test case object
        for (TestCaseObject testCaseObject : testCaseObjects) {
            String testCaseId = testCaseObject.getTestCaseId();
            if (testCaseId.isEmpty()) {
                throw new ApollonBaseException(ApollonErrorKeys.TEST_CASE_ID_IS_REQUIRED, testCaseObject.getName());
            }
            testRunResultMap.put(testCaseId, testCaseObject.getTestRunResult());
        }
        return testRunResultMap;
    }

    /**
     * copy resources files in defined locations to current local folder.
     */
    public static void retrieveResources() {
        retrieveDBConfig();
        retrieveRestConfig();
        retrieveJiraConfig();
        retrieveQCConfig();
    }

    private static void retrieveDBConfig() {
        if (FileOperation.isFileExists(PropertyResolver.getDBConfigFile())) {
            JsonNode dbConfig = JSONContainerFactory.getConfig(PropertyResolver.getDBConfigFile());
            try {
                PropertyResolver.setProperty(PropertyKey.DB_USER.key(), dbConfig.get("user").textValue());
                PropertyResolver.setProperty(PropertyKey.DB_PASSWORD.key(), dbConfig.get("password").textValue());
                PropertyResolver.setProperty(PropertyKey.DB_HOST.key(), dbConfig.get("host").textValue());
                PropertyResolver.setProperty(PropertyKey.DB_NAME.key(), dbConfig.get("service-name").textValue());
                PropertyResolver.setProperty(PropertyKey.DB_TYPE.key(), dbConfig.get("type").textValue());
                PropertyResolver.setProperty(PropertyKey.DB_PORT.key(), dbConfig.get("port").textValue());
            } catch (NullPointerException exception) {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_ERROR, exception, "DB");
            }
        } else {
            trace("DB Connection Config was not found in resource.");
        }
    }

    private static void retrieveRestConfig() {
        if (FileOperation.isFileExists(PropertyResolver.getRESTConfigFile())) {
            try {
                JsonNode restConfig = JSONContainerFactory.getConfig(PropertyResolver.getRESTConfigFile());
                PropertyResolver.setProperty(PropertyKey.REST_USER.key(), restConfig.get("user").textValue());
                PropertyResolver.setProperty(PropertyKey.REST_PASSWORD.key(), restConfig.get("password").textValue());
                PropertyResolver.setProperty(PropertyKey.REST_HOST.key(), restConfig.get("host").textValue());
                PropertyResolver.setProperty(PropertyKey.REST_PAT.key(), restConfig.get("pat").textValue());
            } catch (NullPointerException exception) {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_ERROR, exception, "REST");
            }
        } else {
            trace("REST Connection Config was not found in resource.");
        }
    }

    private static void retrieveJiraConfig() {
        if (PropertyResolver.isJIRAConnectEnabled()) {
            if (FileOperation.isFileExists(PropertyResolver.getJiraConfigFile())) {
                try {
                    JsonNode jiraConfig = JSONContainerFactory.getConfig(PropertyResolver.getJiraConfigFile());
                    PropertyResolver.setProperty(PropertyKey.JIRA_USER.key(), jiraConfig.get("user").textValue());
                    PropertyResolver.setProperty(PropertyKey.JIRA_PASSWORD.key(), jiraConfig.get("password").textValue());
                    PropertyResolver.setProperty(PropertyKey.JIRA_HOST.key(), jiraConfig.get("host").textValue());
                    PropertyResolver.setProperty(PropertyKey.JIRA_PAT.key(), jiraConfig.get("pat").textValue());
                } catch (NullPointerException exception) {
                    throw new ApollonBaseException(ApollonErrorKeys.CONFIG_ERROR, exception, "JIRA");
                }
            } else {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_FILE_NOT_FOUND, PropertyResolver.getJiraConfigFile());
            }
            if (PropertyResolver.isJIRASyncEnabled()) {
                if (FileOperation.isFileExists(PropertyResolver.getJiraExecConfigFile())) {
                    jiraExecutionConfig = JSONContainerFactory.getRunnerConfig(PropertyResolver.getJiraExecConfigFile());
                } else {
                    throw new ApollonBaseException(ApollonErrorKeys.CONFIG_FILE_NOT_FOUND, PropertyResolver.getJiraExecConfigFile());
                }
                if (jiraExecutionConfig.getTestExecutionId().isEmpty()
                        && (jiraExecutionConfig.isFullRun()
                        || jiraExecutionConfig.isFailureRetest())) {
                    throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "For full run or failure retest, the execution id must be provided.");
                }
            }
        }
    }

    private static void retrieveQCConfig() {
        if (PropertyResolver.isQCSyncEnabled()) {
            if (!FileOperation.isFileExists(PropertyResolver.getQCConfigFile())) {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_FILE_NOT_FOUND, PropertyResolver.getQCConfigFile());
            }
        }
    }


    /**
     * Restore Cookies and URL of Web Driver
     */
    @SuppressWarnings("unchecked")
    public static void restoreSessions() {
        WebDriver webDriver = DriverManager.getWebDriver();
        if (Objects.nonNull(FileLocator.findLocalResource("target/logs/cookies.data"))
                && Objects.nonNull(FileLocator.findLocalResource("target/logs/currentURL.data"))) {
            List<Cookie> cookies = (ArrayList<Cookie>) readObject(ArrayList.class, "target/logs/cookies.data");
            String currentURL = readObject(String.class, "target/logs/currentURL.data");
            if (currentURL != null && !currentURL.isEmpty()) {
                webDriver.navigate().to(currentURL);
            }
            if (!cookies.isEmpty()) {
                cookies.forEach(cookie -> webDriver.manage().addCookie(cookie));
            } else {
                throw new ApollonBaseException(ApollonErrorKeys.FAILED_TO_RELOAD_COOKIES);
            }
            if (currentURL != null && !currentURL.isEmpty()) {
                webDriver.navigate().to(currentURL);
            }
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.FAILED_TO_RELOAD_COOKIES);
        }
    }

    /**
     * Store Cookies and URL of Web Driver
     */
    public static void storeSessions() {
        WebDriver webDriver = DriverManager.getWebDriver();
        WriteObject(new ArrayList<>(webDriver.manage().getCookies()), "target/logs/cookies.data");
        WriteObject(webDriver.getCurrentUrl(), "target/logs/currentURL.data");
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
        WriteObject(retryTestCaseID, "target/logs/retryTestCaseID.data");
        WriteObject(stepOrder, "target/logs/stepOrder.data");
    }

    /**
     * load logged test step order number for retry
     *
     * @return number of step order
     */
    public static int loadRetryStepOrder() {
        if (new File("target/logs/stepOrder.data").exists()) {
            return readObject(Integer.class, "target/logs/stepOrder.data");
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
        if (new File("target/logs/retryTestCaseID.data").exists()) {
            return readObject(String.class, "target/logs/retryTestCaseID.data");
        } else {
            return "";
        }
    }

    /**
     * the file named with "xxxx-test-data-global.json" will be loaded automatically
     */
    public static void loadGlobalTestData() {
        if (FileLocator.isResourceFileExists(PropertyResolver.getTestDataLocation())) {
            Path path = FileLocator.findResource(PropertyResolver.getTestDataLocation());
            List<Path> paths = FileLocator.listRegularFilesRecursiveMatchedToName(path.toString(), 5, "testdata-global");
            List<Path> paths1 = FileLocator.listRegularFilesRecursiveMatchedToName(path.toString(), 5, "testData-global");
            paths.addAll(paths1);
            if (paths.size() > 1) {
                throw new ApollonBaseException(ApollonErrorKeys.TEST_DATA_GLOBAL_FILE_SHOULD_BE_UNIQUE, Strings.join("|", paths.toArray()));
            } else if (paths.size() == 1) {
                TestDataContainer.loadGlobalTestData(paths.get(0));
            } else {
                trace("No global test data file detected!");
            }
        }
    }

    public static JSONRunnerConfig getTfsRunnerConfig() {
        return tfsRunnerConfig;
    }
}