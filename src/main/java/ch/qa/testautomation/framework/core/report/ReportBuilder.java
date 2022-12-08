package ch.qa.testautomation.framework.core.report;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.PropertyKey;
import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.utils.DateTimeUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.*;
import ch.qa.testautomation.framework.core.controller.ExternAppController;
import ch.qa.testautomation.framework.core.json.container.JSONRunnerConfig;
import ch.qa.testautomation.framework.core.json.container.JSONStepResult;
import ch.qa.testautomation.framework.core.json.container.JSONTestResult;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.properties.SortedProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.warn;
import static ch.qa.testautomation.framework.common.utils.StringTextUtils.isValid;
import static ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory.*;

public class ReportBuilder {

    private static final List<File> extraAttachment = new LinkedList<>();

    /**
     * startNow to record logs into separate file with test case name as folder
     *
     * @param testRunResult test run result
     */
    public void startRecordingTest(TestRunResult testRunResult) {
        trace("Start recording Test Run ...");
        String folder = PropertyResolver.getTestCaseReportLocation();
        String today = DateTimeUtils.getFormattedDateNow(PropertyResolver.getDataFormat());
        String fileName = folder + today + "/" + testRunResult.getName() + "/" + "report_" + DateTimeUtils.getFormattedLocalTimestamp() + ".log";
        File logFile = new File(fileName);
        logFile.getParentFile().mkdirs();
        trace("Set up log file: " + logFile.getAbsolutePath());
        testRunResult.setLogFilePath(logFile.getAbsolutePath());
    }

    /**
     * stop recording logs and write test case log
     *
     * @param testRunResult test run result
     */
    public void stopRecordingTest(TestRunResult testRunResult) {
        trace("Stop recording Test Run ...");
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
     * @param testCaseObjects test case objects
     */
    public void generateAllureResults(List<TestCaseObject> testCaseObjects) {
        List<JSONTestResult> allureResults = new LinkedList<>();
        for (TestCaseObject testCaseObject : testCaseObjects) {
            String logFilePath = testCaseObject.getTestRunResult().getLogFilePath();
            JSONTestResult jsonTestResult = new JSONTestResult(testCaseObject.getTestRunResult());
            jsonTestResult.setFullName(testCaseObject.getTestCase().getDescription());
            addLabels(jsonTestResult, testCaseObject);//set suiteName
            addLinks(jsonTestResult, testCaseObject);
            jsonTestResult.setHistoryId(buildHistoryId(testCaseObject.getSuiteName() + "." + testCaseObject.getName()));
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
        }
        JSONContainerFactory.regenerateAllureResults(allureResults);
    }

    /**
     * generate final allure html report via existing allure results
     */
    public void generateAllureHTMLReport() {
        String resultsPath = PropertyResolver.getAllureResultsDir();
        String reportPath = PropertyResolver.getAllureReportDir();
        int currentOrder = JSONContainerFactory.getCurrentOrder();
        //get last run history data
        restoreHistory(currentOrder - 1);
        ExternAppController.executeCommand("allure generate " + resultsPath + " -o " + reportPath + "run" + currentOrder);
        archiveResults(currentOrder);
        if (PropertyResolver.isRebaseAllureReport()) {
            rebaseExistingAllureResults();
        }
    }

    /**
     * generate environment properties with every thing
     */
    public void generateEnvironmentProperties() {
        trace("Prepare Environment Property List for Report later.");
        Properties properties = getPropertiesList();
        SortedProperties propertiesSorted = sortProperties(properties);
        String environment = PropertyResolver.getAllureResultsDir() + "environment.properties";
        try {
            propertiesSorted.store(new FileWriter(environment), "Save to environment properties file.");
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, ex, "IO Exception while generate environment file!");
        }
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
                String filePath = PropertyResolver.getAllureResultsDir() + "history/history.json";
                FileOperation.writeStringToFile(historyContent, filePath);
            }
            for (JsonNode result : rebasedAllureResults) {
                FileOperation.writeStringToFile(result.toString(), PropertyResolver.getAllureResultsDir() + result.get("uuid").asText() + "-result.json");
            }
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_READING, ex, tempPath);
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
        String resultsPath = PropertyResolver.getAllureResultsDir();
        try {
            File historyDir = new File(resultsPath + "history/");
            if (new File(resultsPath).exists()) {
                historyDir.mkdir();
                List<String> hisFiles = JSONContainerFactory.getHistoryFiles(order);
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
            warn("IO Exception while restore history file after test run!\n" + ex.getMessage());
        }
    }

    private void addLabels(JSONTestResult jsonTestResult, TestCaseObject testCaseObject) {
        String suiteName = testCaseObject.getPackageName();
        if (suiteName.equals(PropertyResolver.getTestCaseLocation())) {
            suiteName += "default";
            addLabelToResult("suite", suiteName, jsonTestResult);
        }
        testCaseObject.setSuiteName(suiteName);
        addLabelToResult("testClass", testCaseObject.getTestRunResult().getName(), jsonTestResult);
        addLabelToResult("story", testCaseObject.getTestCase().getStory(), jsonTestResult);
        addLabelToResult("epic", testCaseObject.getTestCase().getEpic(), jsonTestResult);
        addLabelToResult("feature", testCaseObject.getTestCase().getFeature(), jsonTestResult);
        addLabelToResult("thread", testCaseObject.getTestRunResult().getThreadName(), jsonTestResult);
    }

    private void addLinks(JSONTestResult jsonTestResult, TestCaseObject testCaseObject) {
        String testCaseId = testCaseObject.getTestCaseId();
        String source = testCaseObject.getTestCase().getSource();
        if (isValid(source)) {
            if (source.equalsIgnoreCase("JIRA")) {
                String url = PropertyResolver.getJiraHost();
                if (isValid(url)) {
                    jsonTestResult.addLink(testCaseId, url + "/browse/" + testCaseId);
                }
            } else if (source.equalsIgnoreCase("TFS") || source.equalsIgnoreCase("AzureDevOps")) {
                JSONRunnerConfig tfsRunnerConfig = TestRunManager.getTfsRunnerConfig();
                if (Objects.nonNull(tfsRunnerConfig)) {
                    jsonTestResult.addLink(testCaseId, tfsRunnerConfig.getTfsConfig());
                }
            }
        }

    }

    private void addLabelToResult(String name, String label, JSONTestResult jsonTestResult) {
        if (label != null && !label.isEmpty()) {
            jsonTestResult.addLabel(name, label);
        }
    }


    /**
     * Prüft, ob alle Properties ausgegeben werden sollen,
     * oder nur jene von der File DefaultTestRunProperties
     *
     * @return Liste mit den benötigten Properties
     */
    private Properties getPropertiesList() {
        Properties properties = new Properties();
        if (PropertyResolver.showAllEnvironmentVariables()) {
            System.getProperties().keySet().forEach(key -> {
                String keyString = key.toString();
                String value = System.getProperty(keyString);
                if (keyString.toLowerCase().contains("password") || keyString.toLowerCase().contains("pat")) {
                    value = "XXXXXXXXXXX";
                }
                properties.setProperty(keyString, value);
            });
        }
        //Enum von PropertyKey durchgehen und die benötigten Properties auslesen
        //und die Werte in die neue propertiesFromFile schreiben
        Arrays.stream(PropertyKey.values()).forEach(propertyKey -> {
            String value = PropertyResolver.getProperty(propertyKey.key());
            if (propertyKey.key().contains("password") || propertyKey.key().contains("pat")) {
                value = "XXXXXXXXXXX";
            }
            if (Objects.nonNull(value)) {
                properties.setProperty(propertyKey.key(), value);
            }
        });
        return properties;
    }

    private SortedProperties sortProperties(Properties properties) {
        SortedProperties sortedProperties = new SortedProperties();
        sortedProperties.putAll(properties);
        return sortedProperties;
    }
}