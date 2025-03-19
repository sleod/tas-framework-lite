package ch.qa.testautomation.tas.core.report;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.PropertyKey;
import ch.qa.testautomation.tas.common.enumerations.TestStatus;
import ch.qa.testautomation.tas.common.utils.DateTimeUtils;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.*;
import ch.qa.testautomation.tas.core.controller.ExternAppController;
import ch.qa.testautomation.tas.core.json.container.JSONDriverConfig;
import ch.qa.testautomation.tas.core.json.container.JSONStepResult;
import ch.qa.testautomation.tas.core.json.container.JSONTestResult;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.apache.commons.collections4.properties.SortedProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;
import static ch.qa.testautomation.tas.core.json.ObjectMapperSingleton.getObjectMapper;
import static ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory.*;

public class ReportBuilder {

    private static final List<File> extraAttachment = new LinkedList<>();
    private static final String EVN_FILE_NAME = "environment.properties";
    private static final String EXECUTOR_FILE_NAME = "executor.json";
    private static final String FRAMEWORK_CONFIG_FILE_NAME = "frameworkConfig.json";
    private static List<String> usedDriverConfigs = null;
    @Getter
    private static int currentOrder;

    /**
     * startNow to record logs into separate file with test case name as folder
     *
     * @param testRunResult test run result
     */
    public void startRecordingTest(TestRunResult testRunResult) {
        info("Start recording Test Run ...");
        String folder = PropertyResolver.getTestCaseReportLocation();
        String today = DateTimeUtils.getFormattedDateNow(PropertyResolver.getDateFormat());
        String fileName = folder + today + "/" + testRunResult.getName() + "/" + Thread.currentThread().getName()
                + "/" + "report_" + DateTimeUtils.getFormattedLocalTimestamp() + ".log";
        File logFile = new File(fileName);
        logFile.getParentFile().mkdirs();
        info("Set up log file: " + logFile.getAbsolutePath());
        testRunResult.setLogFilePath(logFile.getAbsolutePath());
    }

    /**
     * stop recording logs and write test case log
     *
     * @param testRunResult test run result
     */
    public void stopRecordingTest(TestRunResult testRunResult) {
        info("Stop recording Test Run ...");
        StringBuilder logContent = new StringBuilder();
        logContent.append(testRunResult.getBegin()).append("\n");
        for (TestStepResult testStepResult : testRunResult.getStepResults()) {
            String info = testStepResult.getInfo();
            logContent.append(info);
            if (testStepResult.getStatus().equals(TestStatus.FAIL)) {
                logContent.append(testStepResult.getTestFailure().getMessage())
                        .append("\n").append(testStepResult.getTestFailure().getTrace());
            }
        }
        logContent.append(testRunResult.getEnd()).append("\n");
        FileOperation.writeStringToFile(logContent.toString(), testRunResult.getLogFilePath());
    }

    /**
     * generate maven xml report for tfs
     */
    public void generateMavenTestXMLReport() {
        if (TestRunManager.getPerformer().getTestCaseObjects() != null) {
            String folder = PropertyResolver.getTestCaseReportLocation();
            String fileName = folder + "/" + "MavenXMLReport-" + DateTimeUtils.getFormattedLocalTimestamp() + ".xml";
            String backupFile = folder + "/" + "MavenXMLReport-latest.xml";
            MavenReportWriter.generateMavenTestXML(TestRunManager.getPerformer().getTestCaseObjects(), fileName, backupFile);
        }
    }

    /**
     * add extra attachment for single test case run
     *
     * @param attachment extra attachment
     */
    public static void addExtraAttachment4TestCase(File attachment) {
        extraAttachment.add(attachment);
    }

    /**
     * generate default allure results in json files
     *
     * @param testCaseObject test case object
     * @return file paths of result json files
     */
    public List<String> generateAllureResults(TestCaseObject testCaseObject) {
        List<JSONTestResult> allureResults = new LinkedList<>();
        String logFilePath = testCaseObject.getTestRunResult().getLogFilePath();
        JSONTestResult jsonTestResult = new JSONTestResult(testCaseObject.getTestRunResult());
        changeTestCaseNameInResult(jsonTestResult, testCaseObject);
        jsonTestResult.setFullName(testCaseObject.getPackageName() + "." + testCaseObject.getName());//fill full name as required
        addLabels(jsonTestResult, testCaseObject);//add labels
        addLinks(jsonTestResult, testCaseObject);//add links
        addParameters(jsonTestResult, testCaseObject);
        jsonTestResult.setHistoryId(buildHistoryId(testCaseObject.getPackageName() + "." + jsonTestResult.getName()));
        for (TestCaseStep testCaseStep : testCaseObject.getSteps()) {
            //Attachment will be done by construction
            JSONStepResult jsonStepResult = new JSONStepResult(testCaseStep, logFilePath);
            TestStatus testStatus = testCaseStep.getTestStepResult().getStatus();
            if (testStatus.equals(TestStatus.FAIL) || testStatus.equals(TestStatus.BROKEN)) {
                jsonTestResult.setStatusDetails("known", false);
                jsonTestResult.setStatusDetails("muted", false);
                jsonTestResult.setStatusDetails("flaky", false);
                jsonTestResult.setStatusDetails("message", testCaseStep.getTestStepResult().getTestFailure().getMessage());
                jsonTestResult.setStatusDetails("trace", testCaseStep.getTestStepResult().getTestFailure().getTrace());
            }
            jsonTestResult.addStep(jsonStepResult);
        }
        //attach log
        if (isValid(logFilePath)) {
            jsonTestResult.addAttachment(new File(logFilePath));
        }
        //attach video
        String videoFilePath = testCaseObject.getTestRunResult().getVideoFilePath();
        if (isValid(videoFilePath)) {
            jsonTestResult.addAttachment(new File(videoFilePath));
        }
        //attach extra attachments
        if (!extraAttachment.isEmpty()) {
            extraAttachment.forEach(jsonTestResult::addAttachment);
            extraAttachment.clear();
        }
        allureResults.add(jsonTestResult);
        return regenerateAllureResults(allureResults);
    }

    /**
     * generate final allure html report via existing allure results
     */
    public void generateAllureHTMLReport() {
        int currentOrder = getCurrentOrder();
        //get last run history data
        restoreHistory(currentOrder - 1);
        //if run folder exists
        String currReportDir = PropertyResolver.getAllureReportDirectory() + "run" + currentOrder;
        String command = "allure generate ";
        if (FileOperation.isFileExists(currReportDir)) {
            command += "--clean ";
        }
        command += PropertyResolver.getAllureResultsDirectory() + " -o " + currReportDir;
        ExternAppController.executeCommand(command);
        //move results to current run folder
        ReportBuilder.archiveResults();
        if (PropertyResolver.isRebaseAllureReportEnabled()) {
            rebaseExistingAllureResults();
        }
    }

    public static void archiveResults() {
        JSONContainerFactory.archiveResults(getCurrentOrder());
    }

    /**
     * Generate executor json and set current order of run
     */
    public static void generateExecutorJSON() {
        ObjectNode executor = getObjectMapper().createObjectNode();
        String url = System.getProperty(MAIN_BUILD_URL_KEY, "http://localhost:63342/framework/target/allure-report/");
        int buildOrder = 1;
        String content = getExecutorContent();
        JsonNode existingExecutor = null;
        if (!content.isEmpty()) {
            try {
                existingExecutor = getObjectMapper().readTree(content);
            } catch (JsonProcessingException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, content);
            }
        }
        if (existingExecutor != null) {
            buildOrder = existingExecutor.get(BUILD_ORDER_KEY).asInt() + 1;
        }
        String buildName = System.getProperty(BUILD_NAME_KEY, "Automated_Test_Run") + "/#" + buildOrder;
        executor.put("name", PropertyResolver.getSystemUser())
                .put("type", "junit")
                .put("url", url)
                .put(BUILD_ORDER_KEY, buildOrder)
                .put(BUILD_NAME_KEY, buildName)
                .put(REPORT_URL, url + "run" + buildOrder)
                .put(REPORT_NAME, "Allure Report of Test Run" + buildOrder);
        FileOperation.writeStringToFile(executor.toString(), PropertyResolver.getAllureResultsDirectory() + EXECUTOR_FILE_NAME);
        currentOrder = buildOrder;
    }

    public void generateReports() {
        generateFrameworkConfig();
        generateAllureHTMLReport();
        generateMavenTestXMLReport();
    }

    /**
     * generate environment properties with every thing
     */
    public static void generateEnvironmentProperties() {
        info("Prepare Environment Property List for Report later.");
        Properties properties = new Properties();
        System.getenv().forEach(properties::setProperty);
        SortedProperties propertiesSorted = sortProperties(properties);
        String environment = PropertyResolver.getAllureResultsDirectory() + EVN_FILE_NAME;
        try {
            propertiesSorted.store(new FileWriter(environment), "Save to environment properties file.");
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "IO Exception while generate environment file!");
        }
    }

    public void generateFrameworkConfig() {
        info("Prepare Framework Configuration List for Report later.");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode frameworkConfig = mapper.createObjectNode();
        //summary
        ObjectNode summary = mapper.createObjectNode();
        summary.put("Project", new File(System.getProperty("user.dir")).getName());
        summary.put("Execution", DateTimeUtils.getFormattedLocalTimestamp());
        summary.put("Platform", new File(System.getProperty("os.name")).getName());
        frameworkConfig.set("Summary", summary);
        //properties
        Arrays.stream(PropertyKey.values()).forEach(propertyKey -> {
            String keyName = propertyKey.key();
            String propValue = PropertyResolver.getProperty(keyName);
            String sysValue = System.getProperty(keyName);
            String value = isValid(sysValue) ? sysValue : propValue;
            String keyPrefix = keyName.substring(0, keyName.indexOf("."));
            if ((keyName.contains("password") || keyName.contains("pat")) && isValid(value)) {
                value = value.substring(0, 2) + "******" + value.substring(value.length() - 2);
            }
            JsonNode config = null;
            String configName = "";

            if (PropertyKey.JIRA_CONFIG.equals(propertyKey) && FileOperation.isFileExists(PropertyResolver.getJiraConfigFile())) {
                //jira config
                value = PropertyResolver.getJiraConfigFile();
                config = JSONContainerFactory.getConfig(PropertyResolver.getJiraConfigFile());
                configName = "JIRA Config";
            } else if (PropertyKey.TFS_RUNNER_CONFIG.equals(propertyKey) && FileOperation.isFileExists(PropertyResolver.getTFSRunnerConfigFile())) {
                //tfs config
                value = PropertyResolver.getTFSRunnerConfigFile();
                config = JSONContainerFactory.getConfig(PropertyResolver.getTFSRunnerConfigFile());
                ((ObjectNode) config.get("tfsConfig")).put("pat", "****");
                configName = "TFS Config";
            } else if (PropertyKey.QC_CONFIG.equals(propertyKey) && FileOperation.isFileExists(PropertyResolver.getQCConfigFile())) {
                //qc config
                value = PropertyResolver.getQCConfigFile();
                config = JSONContainerFactory.getConfig(PropertyResolver.getQCConfigFile());
                ((ObjectNode) config).put("password", "****");
                configName = "QC Config";
            } else if (PropertyKey.REST_CONFIG.equals(propertyKey) && !PropertyResolver.getRestHost().isEmpty()) {
                //rest config
                value = PropertyResolver.getRestConfigFile();
                config = JSONContainerFactory.getConfig(PropertyResolver.getRestConfigFile());
                ((ObjectNode) config).put("password", "****");
                ((ObjectNode) config).put("pat", "****");
                configName = "REST Config";
            } else if (PropertyKey.DB_CONFIG.equals(propertyKey) && !PropertyResolver.getDBHost().isEmpty()) {
                //db config
                value = PropertyResolver.getDBConfigFile();
                config = JSONContainerFactory.getConfig(PropertyResolver.getDBConfigFile());
                ((ObjectNode) config).put("password", "****");
                configName = "DB Config";
            } else if (PropertyKey.REPORT_SERVICE_RUNNER_CONFIG.equals(propertyKey) && FileOperation.isFileExists(PropertyResolver.getReportServiceRunnerConfigFile())) {
                //allure config
                value = PropertyResolver.getReportServiceRunnerConfigFile();
                config = JSONContainerFactory.getConfig(PropertyResolver.getReportServiceRunnerConfigFile()).get("allureServiceConfig");
                configName = "ALLURE Report Service Config";
            } else if (PropertyKey.DRIVER_DOWNLOAD_CONFIG.equals(propertyKey) && FileOperation.isFileExists(PropertyResolver.getDriverDownloadConfigFile())) {
                //driver download config
                value = PropertyResolver.getDriverDownloadConfigFile();
                config = JSONContainerFactory.getConfig(PropertyResolver.getDriverDownloadConfigFile());
                configName = "Driver Download Config";
            }
            if (isValid(value)) {
                //register prefix
                frameworkConfig.putIfAbsent(keyPrefix, mapper.createObjectNode());
                ((ObjectNode) frameworkConfig.get(keyPrefix)).put(keyName, value);
            }
            if (isValid(configName) && isValid(config)) {
                //add config content
                frameworkConfig.set(configName, config);
            }
        });

        //mobile driver
        String folder = PropertyResolver.getMobileDriverConfigLocation();
        //load used driver configs
        getUsedDriverConfigs().forEach(path -> frameworkConfig.set(FileOperation.getFileName(path), JSONContainerFactory.getConfig(path)));

        //write file
        FileOperation.writeStringToFile(frameworkConfig.toString(), PropertyResolver.getAllureResultsDirectory() + FRAMEWORK_CONFIG_FILE_NAME);
    }

    public static List<String> getUsedDriverConfigs() {
        if (Objects.isNull(usedDriverConfigs)) {
            usedDriverConfigs = new LinkedList<>();
        }
        return usedDriverConfigs;
    }

    public static void addUsedConfigs(String filePath) {
        getUsedDriverConfigs().add(filePath);
    }

    /**
     * rebase existing allure result because of diff on History id
     */
    private void rebaseExistingAllureResults() {
        String historyContent = getHistoryContent();
        List<JsonNode> rebasedAllureResults = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        String tempPath = "";
        try {
            for (String filePath : getAllureResults()) {
                tempPath = filePath;
                JsonNode result = mapper.readTree(FileOperation.readFileToLinedString(filePath));
                String historyId = result.get("historyId").asText();
                String rebaseHisId = buildHistoryId(result.get("labels").get(0).get("value").asText() + "." + result.get("name").asText());
                if (!rebaseHisId.equals(historyId)) {
                    historyContent = historyContent.replace(historyId, rebaseHisId);
                    ((ObjectNode) result).put("historyId", rebaseHisId);
                }
                rebasedAllureResults.add(result);
            }

            if (!historyContent.isEmpty()) {
                String filePath = PropertyResolver.getAllureResultsDirectory() + "history/history.json";
                FileOperation.writeStringToFile(historyContent, filePath);
            }
            for (JsonNode result : rebasedAllureResults) {
                FileOperation.writeStringToFile(result.toString(), PropertyResolver.getAllureResultsDirectory() + result.get("uuid").asText() + "-result.json");
            }
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_READING, ex, tempPath);
        }
    }

    /**
     * build history id
     *
     * @param text text argument
     * @return history id
     */
    private String buildHistoryId(String text) {
        return PropertyResolver.encodeBase64(text);
    }

    /**
     * restore history folder case existence
     */
    private void restoreHistory(int order) {
        String resultsPath = PropertyResolver.getAllureResultsDirectory();
        try {
            File historyDir = new File(resultsPath + "history/");
            if (new File(resultsPath).exists()) {
                historyDir.mkdir();
                List<String> hisFiles = getHistoryFiles(order);
                if (hisFiles.isEmpty()) {
                    historyDir.delete();
                } else {
                    for (String filePath : hisFiles) {
                        File source = new File(filePath);
                        Files.copy(source.toPath(), new File(historyDir + "/" + source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException ex) {
            info("IO Exception while restore history file after test run!\n" + ex.getMessage());
        }
    }

    private void addLabels(JSONTestResult jsonTestResult, TestCaseObject testCaseObject) {
        String suiteName = testCaseObject.getPackageName();
        if (PropertyResolver.getTestCaseLocation().equals(suiteName + "/")) {
            suiteName = "default";
        }
        suiteName = suiteName.replace(PropertyResolver.getTestCaseLocation(), "");//remove 'testCases/'
        testCaseObject.setSuiteName(suiteName);
        addLabelToResult("suite", suiteName, jsonTestResult);
        addLabelToResult("testClass", testCaseObject.getTestRunResult().getName(), jsonTestResult);
        addLabelToResult("story", testCaseObject.getTestCase().getStory(), jsonTestResult);
        addLabelToResult("epic", testCaseObject.getTestCase().getEpic(), jsonTestResult);
        addLabelToResult("feature", testCaseObject.getTestCase().getFeature(), jsonTestResult);
        addLabelToResult("thread", testCaseObject.getTestRunResult().getThreadName(), jsonTestResult);
    }

    private void addParameters(JSONTestResult jsonTestResult, TestCaseObject testCaseObject) {
        if (!testCaseObject.getTestRunResult().getParameters().isEmpty()) {
            testCaseObject.getTestRunResult().getParameters().forEach((key, value) -> jsonTestResult.addParameter(key, value.toString()));
        }
    }

    private void addLinks(JSONTestResult jsonTestResult, TestCaseObject testCaseObject) {
        String testCaseId = testCaseObject.getTestCaseId();
        String source = testCaseObject.getTestCase().getSource();
        //source and test case id must be valid
        if (isValid(source) && isValid(testCaseId) && !testCaseId.equals("-")) {
            if (source.equalsIgnoreCase("JIRA")) {
                String url = PropertyResolver.getJiraHost();
                if (isValid(url)) {
                    jsonTestResult.addLink(testCaseId, url + "/browse/" + testCaseId);
                }
            }
        }
    }

    private void addLabelToResult(String name, String label, JSONTestResult jsonTestResult) {
        if (label != null && !label.isEmpty()) {
            jsonTestResult.addLabel(name, label);
        }
    }

    private static SortedProperties sortProperties(Properties properties) {
        SortedProperties sortedProperties = new SortedProperties();
        sortedProperties.putAll(properties);
        return sortedProperties;
    }

    private void changeTestCaseNameInResult(JSONTestResult result, TestCaseObject testCaseObject) {
        String typ = testCaseObject.getTestType().type();
        //add suffix to test case name with Platform+Version+devices
        String testCaseName = result.getName();
        if (typ.startsWith("web") && Objects.nonNull(DriverManager.getRemoteWebDriverProvider())) {
            JSONDriverConfig config = DriverManager.getRemoteWebDriverProvider().getConfig();
            testCaseName += " (" + DriverManager.getCurrentPlatform() + "-"
                    + config.getPlatformVersion() + "-" + config.getBrowserName() + ")";
        }
        result.setName(testCaseName);
    }
}