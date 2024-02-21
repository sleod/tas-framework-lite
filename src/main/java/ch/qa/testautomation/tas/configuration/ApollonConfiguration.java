package ch.qa.testautomation.tas.configuration;

import ch.qa.testautomation.tas.common.enumerations.DownloadStrategy;
import org.apache.logging.log4j.Level;

import static ch.qa.testautomation.tas.common.enumerations.PropertyKey.*;

public class ApollonConfiguration {

    /**
     * set output location for allure report
     *
     * @param value location like default: "target/Reports/"
     */
    public ApollonConfiguration setTestCaseReportLocation(String value) {
        PropertyResolver.setProperty(TESTCASE_REPORT_DIR.key(), value);
        return this;
    }

    /**
     * set test case file location in resources folder
     *
     * @param value location like default "testCases/"
     */
    public ApollonConfiguration setTestCaseLocation(String value) {
        PropertyResolver.setProperty(TESTCASE_LOCATION.key(), value);
        return this;
    }

    /**
     * set test data file location in resources folder
     *
     * @param value location like default: "testData/"
     */
    public ApollonConfiguration setTestDataLocation(String value) {
        PropertyResolver.setProperty(TESTDATA_LOCATION.key(), value);
        return this;
    }

    /**
     * set driver binary file location in resources folder
     *
     * @param value location like default: "webDrivers/"
     */
    public ApollonConfiguration setWebDriverBinLocation(String value) {
        PropertyResolver.setProperty(WEBDRIVER_BIN_LOCATION.key(), value);
        return this;
    }

    /**
     * set driver config file location in resources folder
     *
     * @param value location like default: "driverConfig/"
     */
    public ApollonConfiguration setDriverConfigLocation(String value) {
        PropertyResolver.setProperty(DRIVER_CONFIG_LOCATION.key(), value);
        return this;
    }

    /**
     * set page definition (selectors) file location in resources folder
     *
     * @param value location like default: "pageDefinitions/"
     */
    public ApollonConfiguration setPageConfigLocation(String value) {
        PropertyResolver.setProperty(PAGE_CONFIG_LOCATION.key(), value);
        return this;
    }

    /**
     * set tess data file location in resources folder for OCR text recognition
     *
     * @param value location like default: "tessdata/"
     */
    public ApollonConfiguration setOCRTessDataLocation(String value) {
        PropertyResolver.setProperty(OCR_TESSDATA_LOCATION.key(), value);
        return this;
    }

    /**
     * set local download location, default is empty means using local ${user.home}/Downloads/
     *
     * @param value location can be set like: "D:/TEMP/downloads/"
     */
    public ApollonConfiguration setDownloadLocation(String value) {
        if (!value.isEmpty()) {
            PropertyResolver.setProperty(DOWNLOAD_LOCATION.key(), value);
        }
        return this;
    }

    /**
     * set screenshot format
     *
     * @param value like png jpeg
     */
    public ApollonConfiguration setScreenshotFormat(String value) {
        PropertyResolver.setProperty(SCREENSHOT_FORMAT.key(), value);
        return this;
    }

    /**
     * set package name in java folder, where the test automation implementations are
     *
     * @param value like "com.test.automation"
     */
    public ApollonConfiguration setTestautomationPackage(String value) {
        PropertyResolver.setProperty(TEST_AUTOMATION_PACKAGE.key(), value);
        return this;
    }

    /**
     * set date format
     *
     * @param value like "yyyy-MM-dd"
     */
    public ApollonConfiguration setDateFormat(String value) {
        PropertyResolver.setProperty(DATE_FORMAT.key(), value);
        return this;
    }

    /**
     * toggle if generate video
     *
     * @param value boolean
     */
    public ApollonConfiguration setGenerateVideoEnabled(boolean value) {
        PropertyResolver.setProperty(GENERATE_VIDEO.key(), String.valueOf(value));
        return this;
    }

    /**
     * set driver download strategy
     *
     * @param driverDownloadStrategy {@link DownloadStrategy}
     */
    public ApollonConfiguration setDriverDownloadStrategy(DownloadStrategy driverDownloadStrategy) {
        PropertyResolver.setProperty(DRIVER_DOWNLOAD_STRATEGY.key(), driverDownloadStrategy.name());
        return this;
    }

    /**
     * set docker web testing config file
     *
     * @param value file name
     */
    public ApollonConfiguration setDriverDownloadConfig(String value) {
        PropertyResolver.setProperty(DRIVER_DOWNLOAD_CONFIG.key(), value);
        return this;
    }

    /**
     * set video format
     *
     * @param value like avi mpeg
     */
    public ApollonConfiguration setVideoFormat(String value) {
        PropertyResolver.setProperty(VIDEO_FORMAT.key(), value);
        return this;
    }

    /**
     * toggle if driver restart after every single test case execution
     *
     * @param value boolean, default true
     */
    public ApollonConfiguration setRestartDriverAfterExecutionEnabled(boolean value) {
        PropertyResolver.setProperty(RUN_DRIVER_RESTART.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if test execution stop on fail case immediately and skip rest steps
     *
     * @param value boolean, default true
     */
    public ApollonConfiguration setStopOnErrorEnabled(boolean value) {
        PropertyResolver.setProperty(RUN_STOP_ON_ERROR.key(), String.valueOf(value));
        return this;
    }

    /**
     * set meta filter for test case selection
     *
     * @param value like "+CI, +Demo" in one string separated with ','
     */
    public ApollonConfiguration setMetaFilter(String value) {
        PropertyResolver.setProperty(RUN_META_FILTER.key(), value);
        return this;
    }

    /**
     * set test case file extension
     *
     * @param value default ".tas"
     */
    public ApollonConfiguration setTestCaseFileExtension(String value) {
        PropertyResolver.setProperty(TESTCASE_FILE_EXTENSION.key(), value);
        return this;
    }

    /**
     * toggle if close browser in fail case
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setKeepBrowserOnErrorEnabled(boolean value) {
        PropertyResolver.setProperty(DEBUG_KEEP_BROWSER.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if clean up allure results on server for online allure report
     * if true, allureServiceConfig will be required
     *
     * @param value boolean, default true
     */
    public ApollonConfiguration setCleanUpResultsEnabled(boolean value) {
        PropertyResolver.setProperty(ALLURE_REPORT_CLEANUP.key(), String.valueOf(value));
        return this;
    }

    /**
     * Set level for the Apollon log level
     *
     * @param logLevel logLevel
     */
    public ApollonConfiguration setLogLevelApollon(Level logLevel) {
        PropertyResolver.setProperty(LOG_LEVEL_APOLLON.key(), logLevel.name());
        return this;
    }

    /**
     * Set level for the Apollon log level
     *
     * @param levelName levelName
     */
    public ApollonConfiguration setLogLevelApollon(String levelName) {
        return setLogLevelApollon(Level.getLevel(levelName));
    }

    /**
     * toggle if synchronize test result back to jira,
     * if true, jira configs will be required
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setJIRASyncEnabled(boolean value) {
        PropertyResolver.setProperty(RUN_JIRA_SYNC.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if synchronize test result back to HP QC,
     * if true, qc configs will be required
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setSyncToQCEnabled(boolean value) {
        PropertyResolver.setProperty(RUN_QC_SYNC.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if upload allure results onto server for online allure report,
     * if true, the allureServiceConfig will be required!
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setAllureReportServiceEnabled(boolean value) {
        PropertyResolver.setProperty(ALLURE_REPORT_SERVICE.key(), String.valueOf(value));
        return this;
    }

    /**
     * set allure report location in project folder
     *
     * @param value like default: "target/allure-report/"
     */
    public ApollonConfiguration setAllureReportDirectory(String value) {
        PropertyResolver.setProperty(ALLURE_REPORT_LOCATION.key(), value);
        return this;
    }

    /**
     * set allure results location in project folder
     *
     * @param value like default: "target/allure-results/"
     */
    public ApollonConfiguration setAllureResultsDirectory(String value) {
        PropertyResolver.setProperty(ALLURE_RESULTS_LOCATION.key(), value);
        return this;
    }

    /**
     * toggle if rebase allure report from history
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setRebaseAllureReportEnabled(boolean value) {
        PropertyResolver.setProperty(ALLURE_REPORT_REBASE.key(), String.valueOf(value));
        return this;
    }

    /**
     * set config file for allure report online server
     *
     * @param value like default: "reportServiceRunnerConfig.json"
     */
    public ApollonConfiguration setReportServiceRunnerConfigFile(String value) {
        PropertyResolver.setProperty(REPORT_SERVICE_RUNNER_CONFIG.key(), value);
        return this;
    }

    /**
     * toggle if browser driver use headless mode
     *
     * @param value boolean, default true
     */
    public ApollonConfiguration setHeadLessModeEnabled(boolean value) {
        PropertyResolver.setProperty(DRIVER_BROWSER_HEADLESS.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if browser driver use full screen on start
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setBrowserFullscreenEnabled(boolean value) {
        PropertyResolver.setProperty(DRIVER_BROWSER_FULLSCREEN.key(), String.valueOf(value));
        return this;
    }

    /**
     * set browser name for web driver to start
     *
     * @param value like default: "chrome"
     */
    public ApollonConfiguration setWebDriverName(String value) {
        PropertyResolver.setWebDriverName(value);
        return this;
    }

    /**
     * set screen size of browser on start
     *
     * @param value like default: "1920,1080" in one string separated with ','
     */
    public ApollonConfiguration setBrowserScreenSize(String value) {
        PropertyResolver.setProperty(BROWSER_SCREEN_SIZE.key(), value);
        return this;
    }

    /**
     * set global wait time of web driver
     *
     * @param sec int in seconds like default: 6
     */
    public ApollonConfiguration setDriverWaitTimeout(int sec) {
        PropertyResolver.setProperty(DRIVER_WAIT_TIMEOUT.key(), String.valueOf(sec));
        return this;
    }

    /**
     * toggle if test run use demo mode with colored bordered element after selection
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setDemoModeEnabled(boolean value) {
        PropertyResolver.setProperty(DEMO_MODE_ENABLED.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if retry by next same test execution start with fail step directly
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setRetryOnErrorEnabled(boolean value) {
        PropertyResolver.setProperty(RETRY_MODE_ENABLED.key(), String.valueOf(value));
        return this;
    }

    /**
     * set in case retry enable for the re-execution how many step back from the failed step to start with
     *
     * @param value int default 1
     */
    public ApollonConfiguration setRetryOverSteps(int value) {
        PropertyResolver.setProperty(RETRY_OVER_STEPS.key(), String.valueOf(value));
        return this;
    }

    /**
     * set test environment for picking up test data automatically
     * attention: the given environment should have the folder with same name in resources test data folder
     *
     * @param value like: "DEV", "SYT" ...
     */
    public ApollonConfiguration setTestEnvironment(String value) {
        PropertyResolver.setProperty(TEST_ENVIRONMENT.key(), value);
        return this;
    }

    /**
     * set start url for run
     *
     * @param value url
     */
    public ApollonConfiguration setStartUrl(String value) {
        PropertyResolver.setProperty(RUN_START_URL.key(), value);
        return this;
    }

    /**
     * set config file for DB connection
     *
     * @param value like default: "dbConfig.json"
     */
    public ApollonConfiguration setDBConfigFile(String value) {
        PropertyResolver.setProperty(DB_CONFIG.key(), value);
        return this;
    }

    /**
     * set config file for REST connection
     *
     * @param value like default: "restConfig.json"
     */
    public ApollonConfiguration setRestConfigFile(String value) {
        PropertyResolver.setProperty(REST_CONFIG.key(), value);
        return this;
    }

    /**
     * set config file for TFS connection
     *
     * @param value like default: "tfsRunnerConfig.json"
     */
    public ApollonConfiguration setTFSRunnerConfigFile(String value) {
        PropertyResolver.setProperty(TFS_RUNNER_CONFIG.key(), value);
        return this;
    }

    /**
     * set configuration id in tfs for test plan
     *
     * @param value string like "45635"
     */
    public ApollonConfiguration setTFSConfigurationId(String value) {
        PropertyResolver.setProperty(TFS_CONFIGURATION_ID.key(), value);
        return this;
    }

    /**
     * set config file for JIRA connection
     *
     * @param value like default: "jiraConfig.json"
     */
    public ApollonConfiguration setJiraConfigFile(String value) {
        PropertyResolver.setProperty(JIRA_CONFIG.key(), value);
        return this;
    }

    /**
     * set config file for QC connection
     *
     * @param value like default: "qcConfig.json"
     */
    public ApollonConfiguration setQCConfigFile(String value) {
        PropertyResolver.setProperty(QC_CONFIG.key(), value);
        return this;
    }

    /**
     * set config file for JIRA Client Initialization
     *
     * @param value like default: "jiraExecutionConfig.json"
     */
    public ApollonConfiguration setJiraExecutionConfig(String value) {
        PropertyResolver.setProperty(JIRA_EXEC_CONFIG.key(), value);
        return this;
    }

    /**
     * set max number of threas for remote parallel execution
     *
     * @param value int number
     */
    public ApollonConfiguration setRemoteExecutionMaxThreads(int value) {
        PropertyResolver.setProperty(EXECUTION_REMOTE_THREAD_MAX.key(), String.valueOf(value));
        return this;
    }

    /**
     * set edit  application for use
     *
     * @param value like: "notepad", "notepad++"
     */
    public ApollonConfiguration setTextEditor(String value) {
        PropertyResolver.setProperty(TEXT_EDITOR.key(), value);
        return this;
    }

    /**
     * toggle if open pdf file in system reader after download
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setOpenPDFInSystemReaderEnabled(boolean value) {
        PropertyResolver.setProperty(OPEN_PDF_IN_SYSTEM_READER.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if allow simple string parameter in test step
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setSimpleStringParameterEnabled(boolean value) {
        PropertyResolver.setProperty(SIMPLE_STRING_PARAMETER_ALLOWED.key(), String.valueOf(value));
        return this;
    }
}
