package ch.raiffeisen.testautomation.framework.core.report;

import ch.raiffeisen.testautomation.framework.common.IOUtils.FileOperation;
import ch.raiffeisen.testautomation.framework.common.enumerations.PropertyKey;
import ch.raiffeisen.testautomation.framework.common.enumerations.TestStatus;
import ch.raiffeisen.testautomation.framework.common.logging.Screenshot;
import ch.raiffeisen.testautomation.framework.common.utils.TimeUtils;
import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import ch.raiffeisen.testautomation.framework.core.component.TestCaseObject;
import ch.raiffeisen.testautomation.framework.core.component.TestCaseStep;
import ch.raiffeisen.testautomation.framework.core.component.TestRunResult;
import ch.raiffeisen.testautomation.framework.core.component.TestStepResult;
import ch.raiffeisen.testautomation.framework.core.controller.ExternAppController;
import ch.raiffeisen.testautomation.framework.core.json.container.JSONStepResult;
import ch.raiffeisen.testautomation.framework.core.json.container.JSONTestResult;
import ch.raiffeisen.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.raiffeisen.testautomation.framework.core.report.allure.ReportBuilderAllureService;
import net.sf.json.JSONObject;
import org.apache.commons.collections4.properties.SortedProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.error;
import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.warn;

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

    }

    /**
     * generate final allure html report via existing allure results
     */
    public static void generateAllureHTMLReport() {
        restoreHistory();
        generateEnvironmentProperties();
        generateAllureReport();
        if (PropertyResolver.isRebaseAllureReport()) {
            rebaseExistingAllureResults();
        }
    }

    /**
     * Execute system command to generate allure report using allure executable
     */
    public static void generateAllureReport() {

        String resultsPath = PropertyResolver.getAllureResultsDir();
        String reportPath = PropertyResolver.getAllureReportDir();
        if (PropertyResolver.isAllureReportService()) {

            ReportBuilderAllureService service = new ReportBuilderAllureService();
            service.generateAllureReportOnService();
        } else {

            ExternAppController.executeCommand("allure generate " + resultsPath + " --clean -o " + reportPath);
        }
    }

    /**
     * rebase existing allure result because of diff on History id
     */
    @SuppressWarnings("unchecked")
    private static void rebaseExistingAllureResults() {
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
        try {
            if (!historyContent.isEmpty()) {
                String filePath = PropertyResolver.getAllureResultsDir() + "/history/history.json";
                FileOperation.writeBytesToFile(historyContent.getBytes(), new File(filePath));
            }
            for (JSONObject result : rebasedAllureResults) {
                FileOperation.writeBytesToFile(result.toString().getBytes(), new File(PropertyResolver.getAllureResultsDir() + "/" + result.getString("uuid") + "-result.json"));
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
    private static void restoreHistory() {
        String reportDir = PropertyResolver.getAllureResultsDir();
        try {
            if (new File(reportDir).exists()) {
                String historyDir = reportDir + "/history";
                new File(historyDir).mkdir();
                for (String filePath : JSONContainerFactory.getHistoryFiles()) {
                    File source = new File(filePath);
                    Files.copy(source.toPath(), new File(historyDir + "/" + source.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
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

    /**
     * generate environment properties with every thing
     */
    private static void generateEnvironmentProperties() {

        Properties properties = getPropertiesList();

        SortedProperties propertiesSorted = sortProperties(properties);

        String environment = PropertyResolver.getAllureResultsDir() + "/environment.properties";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(environment);
            propertiesSorted.store(fileWriter, "Save to environment properties file.");
        } catch (IOException ex) {
            warn("IO Exception while generate environment file after test run!\n" + ex.getMessage());
        }finally {
            try {
                if(fileWriter != null){
                    fileWriter.close();
                }
            } catch (IOException e) {
                error(e);
            }
        }
    }

    /**
     * Prueft ob alle Properties ausgegeben werden sollen,
     * oder nur jene von der File DefaultTestRunProperties
     * @return Liste mit den benoetigten Properties
     */
    private static Properties getPropertiesList() {

        Properties properties = System.getProperties();

        if (PropertyResolver.showAllEnviromentVariables()) {

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
        properties.forEach(sortedProperties::put);

        return sortedProperties;
    }
}