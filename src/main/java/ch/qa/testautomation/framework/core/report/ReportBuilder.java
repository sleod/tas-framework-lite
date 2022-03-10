package ch.qa.testautomation.framework.core.report;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.PropertyKey;
import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.logging.Screenshot;
import ch.qa.testautomation.framework.common.utils.TimeUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.TestCaseObject;
import ch.qa.testautomation.framework.core.component.TestCaseStep;
import ch.qa.testautomation.framework.core.component.TestRunResult;
import ch.qa.testautomation.framework.core.component.TestStepResult;
import ch.qa.testautomation.framework.core.controller.ExternAppController;
import ch.qa.testautomation.framework.core.json.container.JSONStepResult;
import ch.qa.testautomation.framework.core.json.container.JSONTestResult;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.collections4.properties.SortedProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.error;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.warn;
import static ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory.*;

public class ReportBuilder {

    private static final List<File> extraAttachment = new LinkedList<>();

    /**
     * startNow to record logs into separate file with test case name as folder
     *
     * @param testRunResult test run result
     */
    public static void startRecordingTest(TestRunResult testRunResult) {
        String folder = PropertyResolver.getDefaultTestCaseReportLocation();
        String today = TimeUtils.getFormattedDateNow(PropertyResolver.getDefaultDataFormat());
        String fileName = folder + today + "/" + testRunResult.getName() + "/" + "report_" + TimeUtils.getFormattedLocalTimestamp() + ".log";
        File logFile = new File(fileName);
        logFile.getParentFile().mkdirs();
        testRunResult.setLogFilePath(logFile.getAbsolutePath());
        //append log writer for file log
//        SystemLogger.addFileAppender("default", fileName, testRunResult.getName(), "all");
    }

    /**
     * stop recording logs
     *
     * @param testRunResult test run result
     */
    public static void stopRecordingTest(TestRunResult testRunResult) {
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
        try {
            FileOperation.writeBytesToFile(logContent.toString().getBytes(), new File(testRunResult.getLogFilePath()));
        } catch (IOException ex) {
            error(ex);
        }
    }

    /**
     * generate report with test case objects
     *
     * @param testCaseObjects test case objects
     */
    public static void generateReport(List<TestCaseObject> testCaseObjects) {
        try {
            if (PropertyResolver.getReportViewType().equalsIgnoreCase("default")) {
                generateDefaultAllureResults(testCaseObjects);
            } else if (PropertyResolver.getReportViewType().equalsIgnoreCase("junit")) {
                generateJunitAllureResults(testCaseObjects);
            }
        } catch (IOException ex) {
            warn("IO Exception while generate report after test run!\n" + ex.getMessage());
        }
    }

    /**
     * generate maven xml report for tfs
     *
     * @param testCaseObjects list of test case objects
     */
    public static void generateMavenTestXMLReport(List<TestCaseObject> testCaseObjects) {
        String folder = PropertyResolver.getDefaultTestCaseReportLocation();
        String fileName = folder + "/" + "MavenXMLReport-" + TimeUtils.getFormattedLocalTimestamp() + ".xml";
        String backupFile = folder + "/" + "MavenXMLReport-latest.xml";
        try {
            MavenReportWriter.generateMavenTestXML(testCaseObjects, fileName, backupFile);
        } catch (IOException ex) {
            warn("IOException while generate Maven XML Report!\n" + ex.getMessage());
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
     * @throws IOException io exception
     */
    private static void generateDefaultAllureResults(List<TestCaseObject> testCaseObjects) throws IOException {
        List<JSONTestResult> allureResults = new LinkedList<>();
        for (TestCaseObject testCaseObject : testCaseObjects) {
            String logFilePath = testCaseObject.getTestRunResult().getLogFilePath();
            JSONTestResult jsonTestResult = new JSONTestResult(testCaseObject.getTestRunResult());
            jsonTestResult.setHistoryId(buildHistoryId(testCaseObject.getPackage() + "." + testCaseObject.getName()));
            jsonTestResult.setFullName(testCaseObject.getTestCase().getDescription());
            addLabels(jsonTestResult, testCaseObject);
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
            if (!logFilePath.isEmpty()) {
                jsonTestResult.addAttachment(new File(logFilePath).getName(), "text/plain", logFilePath);
            }
            //attach video
            String videoFilePath = testCaseObject.getTestRunResult().getVideoFilePath();
            if (!videoFilePath.isEmpty()) {
                jsonTestResult.addAttachment(new File(videoFilePath).getName(), "video/mp4", videoFilePath);
            }
            //attach extra attachments
            if (!extraAttachment.isEmpty()) {
                extraAttachment.forEach(file -> jsonTestResult.addAttachment(file.getName(), "text/plain", file.getAbsolutePath()));
                extraAttachment.clear();
            }
            allureResults.add(jsonTestResult);
        }
        JSONContainerFactory.regenerateAllureResults(allureResults);
    }

    /**
     * generate final allure html report via existing allure results
     */
    public static void generateAllureHTMLReport() {
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
    public static void generateEnvironmentProperties() {
        Properties properties = getPropertiesList();
        SortedProperties propertiesSorted = sortProperties(properties);
        new File(PropertyResolver.getAllureResultsDir()).mkdirs();
        String environment = PropertyResolver.getAllureResultsDir() + "environment.properties";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(environment);
            propertiesSorted.store(fileWriter, "Save to environment properties file.");
        } catch (IOException ex) {
            warn("IO Exception while generate environment file!\n" + ex.getMessage());
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                error(e);
            }
        }
    }

    /**
     * rebase existing allure result because of diff on History id
     */
    private static void rebaseExistingAllureResults() {
        String historyContent = getHistoryContent();
        List<JsonNode> rebasedAllureResults = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            for (String filePath : getAllureResults()) {
                JsonNode result =  mapper.readTree(FileOperation.readFileToLinedString(filePath));
                String historyId = result.get("historyId").asText();
                String rebaseHisId = buildHistoryId(result.get("labels").get(0).get("value").asText() + "." + result.get("name").asText());
                if (!rebaseHisId.equals(historyId)) {
                    historyContent = historyContent.replace(historyId, rebaseHisId);
                    ((ObjectNode)result).put("historyId", rebaseHisId);
                }
                rebasedAllureResults.add(result);
            }

            if (!historyContent.isEmpty()) {
                String filePath = PropertyResolver.getAllureResultsDir() + "history/history.json";
                FileOperation.writeBytesToFile(historyContent.getBytes(), new File(filePath));
            }
            for (JsonNode result : rebasedAllureResults) {
                FileOperation.writeBytesToFile(result.toString().getBytes(), new File(PropertyResolver.getAllureResultsDir() + result.get("uuid").asText() + "-result.json"));
            }
        } catch (IOException ex) {
            warn("IOException while rebase existing allure results! \n" + ex.getMessage());
        }
    }

    /**
     * build history id
     *
     * @param text text argument
     * @return history id
     */
    private static String buildHistoryId(String text) {
        return PropertyResolver.encodeBase64(text);
    }

    /**
     * restore history folder case existence
     */
    private static void restoreHistory(int order) {
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

    /**
     * generate allure results in json files for allure report
     *
     * @param testCaseObjects test cases
     */
    private static void generateJunitAllureResults(List<TestCaseObject> testCaseObjects) {
        List<JSONTestResult> allureResults = new LinkedList<>();
        String attachType = "image/" + PropertyResolver.getDefaultScreenshotFormat().toLowerCase();
        testCaseObjects.forEach(testCaseObject -> testCaseObject.getTestRunResult().getStepResults().forEach(stepResult -> {
            JSONTestResult jsonTestResult = new JSONTestResult(stepResult.getName(), testCaseObject.getName() + "@" + stepResult.getName(),
                    testCaseObject.getDescription(), stepResult.getStatus().text(), stepResult.getStart(), stepResult.getStop(), "finished",
                    testCaseObject.getName() + " #" + stepResult.getStepOrder());
            jsonTestResult.addLabel("suite", testCaseObject.getName());
            jsonTestResult.addLabel("testClass", stepResult.getName());
            jsonTestResult.addLabel("testMethod", stepResult.getTestMethod());
            jsonTestResult.setStatusDetails("known", true);
            jsonTestResult.setStatusDetails("muted", false);
            jsonTestResult.setStatusDetails("flaky", false);
            if (stepResult.getStatus().equals(TestStatus.FAIL) || stepResult.getStatus().equals(TestStatus.BROKEN)) {
                jsonTestResult.setStatusDetails("message", stepResult.getTestFailure().getMessage());
                jsonTestResult.setStatusDetails("trace", stepResult.getTestFailure().getTrace());
            } else {
                jsonTestResult.setStatusDetails("message", "Log Info: ");
                jsonTestResult.setStatusDetails("trace", stepResult.getInfo());
            }
            for (Screenshot screenshot : stepResult.getScreenshots()) {
                jsonTestResult.addAttachment(screenshot.getTestCaseName(), attachType, screenshot.getScreenshotFile().getAbsolutePath());
                if (screenshot.hasPageFile()) {
                    jsonTestResult.addAttachment(screenshot.getTestCaseName(), "text/html", screenshot.getPageFile().getAbsolutePath());
                }
            }
            allureResults.add(jsonTestResult);
        }));
        JSONContainerFactory.regenerateAllureResults(allureResults);
    }

    private static void addLabels(JSONTestResult jsonTestResult, TestCaseObject testCaseObject) {
        addLabelToResult("suite", testCaseObject.getPackage(), jsonTestResult);
        addLabelToResult("testClass", testCaseObject.getTestRunResult().getName(), jsonTestResult);
        addLabelToResult("story", testCaseObject.getTestCase().getStory(), jsonTestResult);
        addLabelToResult("epic", testCaseObject.getTestCase().getEpic(), jsonTestResult);
        addLabelToResult("feature", testCaseObject.getTestCase().getFeature(), jsonTestResult);
    }

    private static void addLabelToResult(String name, String label, JSONTestResult jsonTestResult) {
        if (label != null && !label.isEmpty()) {
            jsonTestResult.addLabel(name, label);
        }
    }


    /**
     * Prueft ob alle Properties ausgegeben werden sollen,
     * oder nur jene von der File DefaultTestRunProperties
     *
     * @return Liste mit den benoetigten Properties
     */
    private static Properties getPropertiesList() {
        Properties properties = System.getProperties();
        if (PropertyResolver.showAllEnvironmentVariables()) {
            return properties;
        } else {
            Properties propertiesFromFile = new Properties();
            //Enum von PropertyKey durchgehen und die benoetigten Properties auslesen
            //und die Werte in die neue propertiesFromFile schreiben
            for (PropertyKey key : PropertyKey.values()) {
                String value = properties.getProperty(key.key());
                if (value != null) {
                    propertiesFromFile.setProperty(key.key(), value);
                }
            }
            return propertiesFromFile;
        }
    }

    private static SortedProperties sortProperties(Properties properties) {
        SortedProperties sortedProperties = new SortedProperties();
        sortedProperties.putAll(properties);
        return sortedProperties;
    }
}