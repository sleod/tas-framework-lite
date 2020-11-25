package ch.raiffeisen.testautomation.framework.core.report;

import ch.raiffeisen.testautomation.framework.common.IOUtils.FileOperation;
import ch.raiffeisen.testautomation.framework.common.enumerations.TestStatus;
import ch.raiffeisen.testautomation.framework.common.logging.Screenshot;
import ch.raiffeisen.testautomation.framework.common.utils.TimeUtils;
import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import ch.raiffeisen.testautomation.framework.core.component.TestCaseObject;
import ch.raiffeisen.testautomation.framework.core.component.TestCaseStep;
import ch.raiffeisen.testautomation.framework.core.component.TestRunResult;
import ch.raiffeisen.testautomation.framework.core.component.TestStepResult;
import ch.raiffeisen.testautomation.framework.core.json.container.JSONAttachment;
import ch.raiffeisen.testautomation.framework.core.json.container.JSONStepResult;
import ch.raiffeisen.testautomation.framework.core.json.container.JSONTestResult;
import ch.raiffeisen.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.error;

public class ReportBuilder {

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
//        SystemLogger.removeAppender(testRunResult.getName());
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
     * @throws IOException io exception
     */
    public static void generateReport(List<TestCaseObject> testCaseObjects) throws IOException {
        if (PropertyResolver.getReportViewType().equalsIgnoreCase("default")) {
            generateDefaultAllureResults(testCaseObjects);
        } else if (PropertyResolver.getReportViewType().equalsIgnoreCase("junit")) {
            generateJunitAllureResults(testCaseObjects);
        }
        generateMavenTestXMLReport(testCaseObjects);
    }

    /**
     * generate maven xml report for tfs
     *
     * @param testCaseObjects list of test case objects
     * @throws IOException io exception
     */
    private static void generateMavenTestXMLReport(List<TestCaseObject> testCaseObjects) throws IOException {
        String folder = PropertyResolver.getDefaultTestCaseReportLocation();
        String fileName = folder + "/" + "MavenXMLReport-" + TimeUtils.getFormattedLocalTimestamp() + ".xml";
        String backupFile = folder + "/" + "MavenXMLReport-latest.xml";
        MavenReportWriter.generateMavenTestXML(testCaseObjects, fileName, backupFile);
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
            jsonTestResult.addLabel("suite", testCaseObject.getPackage());
            jsonTestResult.addLabel("testClass", testCaseObject.getTestRunResult().getName());
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
            allureResults.add(jsonTestResult);
        }
        JSONContainerFactory.regenerateAllureResults(allureResults);
        if (PropertyResolver.isRebaseAllureReport()) {
            rebaseExistingAllureResults();
        }
        generateEnvironmentProperties();
        JSONContainerFactory.generateAllureReport();
        restoreHistory();
    }

    /**
     * rebase existing allure result because of diff on History id
     *
     * @throws IOException io exception
     */
    @SuppressWarnings("unchecked")
    private static void rebaseExistingAllureResults() throws IOException {
        String historyContent = JSONContainerFactory.getHistoryContent();
        List<JSONObject> rebasedAllureResults = new LinkedList<>();
        for (String filePath : JSONContainerFactory.getAllureResults()) {
            JSONObject result = JSONObject.fromObject(FileOperation.readFileToLinedString(filePath));
            String historyId = result.getString("historyId");
            String rebaseHisId = buildHistoryId(result.getJSONArray("labels").getJSONObject(0).getString("value") + "." + result.getString("name"));
            if (!rebaseHisId.equals(historyId)) {
                historyContent = historyContent.replace(historyId, rebaseHisId);
                result.replace("historyId", historyId, rebaseHisId);
            }
            rebasedAllureResults.add(result);
        }
        if (!historyContent.isEmpty()) {
            String filePath = PropertyResolver.getAllureResultsDir() + "/history/history.json";
            FileOperation.writeBytesToFile(historyContent.getBytes(), new File(filePath));
        }
        for (JSONObject result : rebasedAllureResults) {
            FileOperation.writeBytesToFile(result.toString().getBytes(), new File(PropertyResolver.getAllureResultsDir() + "/" + result.getString("uuid") + "-result.json"));
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
     *
     * @throws IOException folder not found
     */
    private static void restoreHistory() throws IOException {
        String reportDir = PropertyResolver.getAllureResultsDir();
        if (new File(reportDir).exists()) {
            String historyDir = reportDir + "/history";
            new File(historyDir).mkdir();
            for (String filePath : JSONContainerFactory.getHistoryFiles()) {
                File source = new File(filePath);
                Files.copy(source.toPath(), new File(historyDir + "/" + source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * generate allure results in json files for allure report
     *
     * @param testCaseObjects test cases
     * @throws IOException io
     */
    private static void generateJunitAllureResults(List<TestCaseObject> testCaseObjects) throws IOException {
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
        restoreHistory();
        generateEnvironmentProperties();
        JSONContainerFactory.generateAllureReport();
    }

    /**
     * generate environment properties with every thing
     *
     * @throws IOException io exception
     */
    private static void generateEnvironmentProperties() throws IOException {
        Map<String, String> env = System.getenv();
        Properties properties = System.getProperties();
        env.forEach(properties::setProperty);
        String environment = PropertyResolver.getAllureResultsDir() + "/environment.properties";
        properties.store(new FileWriter(environment), "Save to environment properties file.");
    }
}