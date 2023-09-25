package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.IOUtils.FileLocator;
import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.PropertyKey;
import ch.qa.testautomation.tas.common.enumerations.TestType;
import ch.qa.testautomation.tas.common.logging.ScreenCapture;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.json.container.JSONRunnerConfig;
import ch.qa.testautomation.tas.core.json.container.JSONTestCase;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.core.report.ReportBuilder;
import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;
import ch.qa.testautomation.tas.rest.base.QUERY_OPTION;
import ch.qa.testautomation.tas.rest.jira.connection.JIRARestClient;
import com.beust.jcommander.Strings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.*;
import static ch.qa.testautomation.tas.common.utils.ObjectWriterReader.WriteObject;
import static ch.qa.testautomation.tas.common.utils.ObjectWriterReader.readObject;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;
import static ch.qa.testautomation.tas.configuration.PropertyResolver.getTestCaseLocation;
import static ch.qa.testautomation.tas.core.json.ObjectMapperSingleton.getObjectMapper;

public class TestRunManager {

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
                debug("Given File can not be attach to Test Case!" + filePath +
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
                info("Pickup Test Case: " + jsonTestCase.getName());
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
        debug("Paths found: " + Arrays.toString(paths.toArray()));
        return paths;
    }

    /**
     * init test cases with file paths of json filtered with meta notation
     *
     * @param filePaths   file paths
     * @param metaFilters meta filters
     */
    public static List<TestCaseObject> initTestCases(List<String> filePaths, List<String> metaFilters) {
        List<String> selectedIds;
        if (PropertyResolver.isJIRASyncEnabled()) {
            Map<String, String> executionKeys = jiraExecutionConfig.getTestExecutionIdMap();
            if (jiraExecutionConfig.isFullRun() && !executionKeys.isEmpty()) {//check run config for full run
                selectedIds = new LinkedList<>();
                executionKeys.values().forEach(exeKey -> selectedIds.addAll(getJiraTestCaseIdsInExecution(exeKey, QUERY_OPTION.ALL)));
            } else if (jiraExecutionConfig.isFailureRetest() && !executionKeys.isEmpty()) {//check run config for retest run
                selectedIds = new LinkedList<>();
                executionKeys.values().forEach(exeKey -> selectedIds.addAll(getJiraTestCaseIdsInExecution(exeKey, QUERY_OPTION.EXCEPT_SUCCESS)));
            } else {//check run config for selected ids to run
                selectedIds = null;
            }
            if (Objects.nonNull(selectedIds) && selectedIds.isEmpty()) {
                throw new ApollonBaseException(ApollonErrorKeys.FAIL_ON_GET_TESTCASE_ID_FROM_TFS);
            }
        } else {
            selectedIds = null;
        }
        if (filePaths.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.TEST_CASE_NOT_FOUND, PropertyResolver.getTestCaseLocation());
        }
        if (!metaFilters.isEmpty()) {
            info("Filters: " + Arrays.toString(metaFilters.toArray()));
        } else {
            warn("No Filter is set, all found test case will be executed!");
        }
        if (metaFilters.contains("")) {
            info("meta filters contains empty value!");
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
                    } else {
                        debug("Test Case with id: " + n_testCaseObject.getTestCaseId() + " was not selected in execution config.");
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

    /**
     * Checks if multiple testcases have the same name or id.
     *
     * @param testCaseObjects list of type testCaseObject.
     * @throws ApollonBaseException if duplicate found.
     */
    protected static void checkDuplicateNaming(List<TestCaseObject> testCaseObjects) {
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
     * @param executionKey execution Key
     * @param query_option {@link QUERY_OPTION}
     * @return list of test case ids
     */
    public static List<String> getJiraTestCaseIdsInExecution(String executionKey, QUERY_OPTION query_option) {
        return new JIRARestClient(PropertyResolver.getJiraHost(), PropertyResolver.getJiraPAT()).getTestsInExecution(executionKey, query_option);
    }

    /**
     * load driver while init test case object
     *
     * @param jsonTestCase json test case
     * @param testCaseName test case name
     */
    public static void loadDriver(JSONTestCase jsonTestCase, String testCaseName) {
        info("Load driver for: " + testCaseName);
        //load all driver config
        TestType type = TestType.valueOf(jsonTestCase.getType().toUpperCase());
        switch (type) {
            case WEB_APP://local
                if (!PropertyResolver.isExecutionRemoteParallelEnabled()) {
                    DriverManager.setCurrentPlatform(System.getProperty("os.name"));
                    DriverManager.setupWebDriver();
                    ScreenCapture.setScreenTaker(DriverManager.getWebDriverProvider());
                } else {//remote
                    DriverManager.setupRemoteWebDriver();
                    ScreenCapture.setScreenTaker(DriverManager.getRemoteWebDriverProvider());
                }
                break;
            case REST:
                DriverManager.setupRestDriver();
                DriverManager.setCurrentPlatform(System.getProperty("os.name"));
                break;
            case APP:
                DriverManager.setupNonDriver();
                DriverManager.setCurrentPlatform(System.getProperty("os.name"));
                break;
            default:
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
        if (!PropertyResolver.isTFSSyncEnabled() && !PropertyResolver.isJIRASyncEnabled() || Objects.isNull(selectedIds)) {
            return true;
        } else if (selectedIds.isEmpty()) {
            debug("List of Selected IDs is Empty! No test case can be selected!");
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
                String vNr = (index + 1) < 10 ? "0" + (index + 1) : String.valueOf(index);
                if (!testData.containsKey("seriesNumber") && isValid(testCaseObject.getSeriesNumber())) {
                    String sNr = testCaseObject.getSeriesNumber() + "." + vNr;
                    testData.put("seriesNumber", sNr);
                    testCaseObject.getTestCase().setSeriesNumber(sNr);
                }
                TestCaseObject new_tco = new TestCaseObject(testCaseObject.getTestCase(), Collections.singletonList(testData), " - Var " + vNr);
                new_tco.setFilePath(testCaseObject.getFilePath());
                if (!isFiltered(testData)) {
                    continue;
                }
                if (PropertyResolver.isTFSSyncEnabled() || PropertyResolver.isJIRASyncEnabled()) {
                    if (!testData.containsKey("testCaseId") && testData.get("testCaseId") != null && !testData.get("testCaseId").toString().isEmpty()) {
                        throw new ApollonBaseException(ApollonErrorKeys.TEST_CASE_ID_IS_REQUIRED, testCaseObject.getName());
                    } else {//add tc to list with valid test case id
                        if (!new_tco.getTestType().equals(TestType.MOBILE_APP)) {//normal case
                            Object testCaseId = testData.get("testCaseId");
                            if (Objects.nonNull(testCaseId) && !Objects.equals(testCaseId, "-")) {//avoid execution with '-'
                                new_tco.setTestCaseId(testCaseId.toString());
                                normCases.add(new_tco);
                            } else {//no add to list
                                info(new_tco.getName() + " is ignored because of '-' value in testCaseId.");
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
                if (testCaseObject.getTestCaseId().isEmpty() && !testCaseObject.getTestType().equals(TestType.MOBILE_APP)
                        || testCaseObject.getTestType().equals(TestType.MOBILE_APP) && testCaseObject.getTestCaseIdMap().isEmpty()) {
                    throw new ApollonBaseException(ApollonErrorKeys.TEST_CASE_ID_IS_REQUIRED, testCaseObject.getName());
                } else if (testCaseObject.getTestCaseId().equals("-")) {
                    info(testCaseObject.getName() + " with testcase id '-' will not be executed!");
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
            info("Both selection and exclusion filters are filled with data. Both filters are ignored!");
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
                PropertyResolver.setProperty(PropertyKey.DB_USER.key(), dbConfig.get("user").asText());
                PropertyResolver.setProperty(PropertyKey.DB_PASSWORD.key(), dbConfig.get("password").asText());
                PropertyResolver.setProperty(PropertyKey.DB_HOST.key(), dbConfig.get("host").asText());
                PropertyResolver.setProperty(PropertyKey.DB_NAME.key(), dbConfig.get("instance.name").asText());
                PropertyResolver.setProperty(PropertyKey.DB_TYPE.key(), dbConfig.get("type").asText());
                PropertyResolver.setProperty(PropertyKey.DB_PORT.key(), dbConfig.get("port").asText());
            } catch (NullPointerException exception) {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_ERROR, exception, PropertyResolver.getDBConfigFile());
            }
        } else {
            info("DB Connection Config was not found in resource.");
        }
    }

    private static void retrieveRestConfig() {
        if (FileOperation.isFileExists(PropertyResolver.getRestConfigFile())) {
            try {
                JsonNode restConfig = JSONContainerFactory.getConfig(PropertyResolver.getRestConfigFile());
                PropertyResolver.setProperty(PropertyKey.REST_USER.key(), restConfig.get("user").asText());
                PropertyResolver.setProperty(PropertyKey.REST_PASSWORD.key(), restConfig.get("password").asText());
                PropertyResolver.setProperty(PropertyKey.REST_HOST.key(), restConfig.get("host").asText());
                PropertyResolver.setProperty(PropertyKey.REST_PAT.key(), restConfig.get("pat").asText());
            } catch (NullPointerException exception) {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_ERROR, exception, PropertyResolver.getRestConfigFile());
            }
        } else {
            info("REST Connection Config was not found in resource.");
        }
    }

    private static void retrieveJiraConfig() {
        if (PropertyResolver.isJIRASyncEnabled()) {
            if (FileOperation.isFileExists(PropertyResolver.getJiraConfigFile())) {
                try {
                    JsonNode jiraConfig = JSONContainerFactory.getConfig(PropertyResolver.getJiraConfigFile());
                    PropertyResolver.setProperty(PropertyKey.JIRA_USER.key(), jiraConfig.get("user").asText());
                    PropertyResolver.setProperty(PropertyKey.JIRA_PASSWORD.key(), jiraConfig.get("password").asText());
                    PropertyResolver.setProperty(PropertyKey.JIRA_HOST.key(), jiraConfig.get("host").asText());
                    PropertyResolver.setProperty(PropertyKey.JIRA_PAT.key(), jiraConfig.get("pat").asText());
                } catch (NullPointerException exception) {
                    throw new ApollonBaseException(ApollonErrorKeys.CONFIG_ERROR, exception, PropertyResolver.getJiraConfigFile());
                }
            } else {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_FILE_NOT_FOUND, PropertyResolver.getJiraConfigFile());
            }
            if (FileOperation.isFileExists(PropertyResolver.getJiraExecutionConfigFile())) {
                jiraExecutionConfig = JSONContainerFactory.getRunnerConfig(PropertyResolver.getJiraExecutionConfigFile());
            } else {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_FILE_NOT_FOUND, PropertyResolver.getJiraExecutionConfigFile());
            }
            if (jiraExecutionConfig.getTestExecutionIdMap().isEmpty()
                    && (jiraExecutionConfig.isFullRun()
                    || jiraExecutionConfig.isFailureRetest())) {
                throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "For full run or failure retest, the execution id must be provided.");
            }
        }
    }

    private static void retrieveQCConfig() {
        if (PropertyResolver.isSyncToQCEnabled()) {
            if (!FileOperation.isFileExists(PropertyResolver.getQCConfigFile())) {
                throw new ApollonBaseException(ApollonErrorKeys.CONFIG_FILE_NOT_FOUND, PropertyResolver.getQCConfigFile());
            } else {
                JsonNode qcConfig = JSONContainerFactory.getConfig(PropertyResolver.getQCConfigFile());
                if (!isValid(PropertyResolver.getQCUser()) && Objects.nonNull(qcConfig.get("user")) && isValid(qcConfig.get("user").asText())) {
                    PropertyResolver.setProperty(PropertyKey.QC_USER.key(), qcConfig.get("user").asText());
                }
                if (!isValid(PropertyResolver.getQCPassword()) && Objects.nonNull(qcConfig.get("password")) && isValid(qcConfig.get("password").asText())) {
                    PropertyResolver.setProperty(PropertyKey.QC_PASSWORD.key(), qcConfig.get("password").asText());
                }
                if (!isValid(PropertyResolver.getQCUser())) {
                    throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "QC User not Set! Please disable qc sync or set user!");
                }
                if (!isValid(PropertyResolver.getQCPassword())) {
                    throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "QC Password not Set! Please disable qc sync or set password!");
                }

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
                info("No global test data file detected!");
            }
        }
    }

}