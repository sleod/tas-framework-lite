package ch.qa.testautomation.framework.configuration;

import static ch.qa.testautomation.framework.common.enumerations.PropertyKey.*;

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
     * set external driver resource location, where driver can be downloaded
     * default set to git from TFS: "Git - RCH Framework Solution Items/Java/DriverVersions/"
     * together with resource project: "ap.testtools"
     * <p>
     * Variant: can be set like: "\\\\shareFolder\\projectXXX\\..." for network share folder
     * and the target file should be driver.zip, which exists in folder and all bin files are within
     *
     * @param value location like default: "driverConfig/"
     */
    public ApollonConfiguration setDriverResourceLocation(String value) {
        PropertyResolver.setProperty(RESOURCE_DRIVER_LOCATION.key(), value);
        return this;
    }

    /**
     * ***ONLY for TFS Server relevant***
     * set external driver resource project, where driver can be downloaded
     *
     * @param value location like default: "ap.testtools"
     */
    public ApollonConfiguration setDriverResourceProject(String value) {
        PropertyResolver.setProperty(RESOURCE_PROJECT.key(), value);
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
    public ApollonConfiguration setTestAutomationPackageName(String value) {
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
    public ApollonConfiguration setIsGenerateVideo(boolean value) {
        PropertyResolver.setProperty(GENERATE_VIDEO.key(), String.valueOf(value));
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
    public ApollonConfiguration setIsDriverRestart(boolean value) {
        PropertyResolver.setProperty(RUN_DRIVER_RESTART.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if test execution stop on fail case immediately and skip rest steps
     *
     * @param value boolean, default true
     */
    public ApollonConfiguration setIsStopOnError(boolean value) {
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
    public ApollonConfiguration setTestcaseFileExtension(String value) {
        PropertyResolver.setProperty(TESTCASE_FILE_EXTENSION.key(), value);
        return this;
    }

    /**
     * toggle if close browser in fail case
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setIsKeepBrowserOnError(boolean value) {
        PropertyResolver.setProperty(DEBUG_KEEP_BROWSER.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if clean up allure results on server for online allure report
     * if true, allureServiceConfig will be required
     *
     * @param value boolean, default true
     */
    public ApollonConfiguration setIsCleanUpResults(boolean value) {
        PropertyResolver.setProperty(ALLURE_REPORT_CLEANUP.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if more deep trace information in console
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setIsPrintDebugOutput(boolean value) {
        PropertyResolver.setProperty(DEBUG_TRACE_OUTPUT.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if connect to tfs to get test case or plan information
     * if true, tfs configs will be required
     *
     * @param value boolean
     */
    public ApollonConfiguration setIsConnectToTFS(boolean value) {
        PropertyResolver.setProperty(RUN_TFS_CONNECT.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if synchronize test result back to TFS,
     * if true, tfs config will be required
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setIsSyncToTFS(boolean value) {
        PropertyResolver.setProperty(RUN_TFS_SYNC.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if connect to jira to get test case or plan information
     * if true, jira configs will be required
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setIsConnectToJIRA(boolean value) {
        PropertyResolver.setProperty(RUN_JIRA_CONNECT.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if synchronize test result back to jira,
     * if true, jira configs will be required
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setIsSyncToJIRA(boolean value) {
        PropertyResolver.setProperty(RUN_JIRA_SYNC.key(), String.valueOf(value));
        return this;
    }

    /**
     * set logger class name
     *
     * @param value like default: "SystemLogger"
     */
    public ApollonConfiguration setLoggerName(String value) {
        PropertyResolver.setProperty(LOGGER_NAME.key(), value);
        return this;
    }

    /**
     * toggle if upload allure results onto server for online allure report,
     * if true, the allureServiceConfig will be required!
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setUseAllureReportService(boolean value) {
        PropertyResolver.setProperty(ALLURE_REPORT_SERVICE.key(), String.valueOf(value));
        return this;
    }

    /**
     * set allure report location in project folder
     *
     * @param value like default: "target/allure-report/"
     */
    public ApollonConfiguration setAllureReportLocation(String value) {
        PropertyResolver.setProperty(ALLURE_REPORT_LOCATION.key(), value);
        return this;
    }

    /**
     * set allure results location in project folder
     *
     * @param value like default: "target/allure-results/"
     */
    public ApollonConfiguration setAllureResultsLocation(String value) {
        PropertyResolver.setProperty(ALLURE_RESULTS_LOCATION.key(), value);
        return this;
    }

    /**
     * toggle if rebase allure report from history
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setIsRebaseAllureReport(boolean value) {
        PropertyResolver.setProperty(ALLURE_REPORT_REBASE.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if show all environment variable in report
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setShowAllEnvironmentVariables(boolean value) {
        PropertyResolver.setProperty(ALLURE_REPORT_ALL_ENVIRONMENT_VARIABLES.key(), String.valueOf(value));
        return this;
    }

    /**
     * set config file for allure report online server
     *
     * @param value like default: "reportServiceRunnerConfig.json"
     */
    public ApollonConfiguration setAllureReportServiceRunnerConfig(String value) {
        PropertyResolver.setProperty(REPORT_SERVICE_RUNNER_CONFIG.key(), value);
        return this;
    }

    /**
     * toggle if browser driver use headless mode
     *
     * @param value boolean, default true
     */
    public ApollonConfiguration setUseBrowserHeadLessMode(boolean value) {
        PropertyResolver.setProperty(DRIVER_BROWSER_HEADLESS.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if browser driver use full screen on start
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setUseBrowserFullscreen(boolean value) {
        PropertyResolver.setProperty(DRIVER_BROWSER_FULLSCREEN.key(), String.valueOf(value));
        return this;
    }

    /**
     * set browser name for web driver to start
     *
     * @param value like default: "chrome"
     */
    public ApollonConfiguration setDriverBrowserName(String value) {
        PropertyResolver.setProperty(DRIVER_BROWSER_NAME.key(), value);
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
    public ApollonConfiguration setUseDemoMode(boolean value) {
        PropertyResolver.setProperty(DEMO_MODE_ENABLED.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if retry by next same test execution start with fail step directly
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setUseRetryOnError(boolean value) {
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
     * set config file for DB connection
     *
     * @param value like default: "dbConfig.json"
     */
    public ApollonConfiguration setDBConfig(String value) {
        PropertyResolver.setProperty(DB_CONFIG.key(), value);
        return this;
    }

    /**
     * set config file for REST connection
     *
     * @param value like default: "restConfig.json"
     */
    public ApollonConfiguration setRestConfig(String value) {
        PropertyResolver.setProperty(REST_CONFIG.key(), value);
        return this;
    }

    /**
     * set config file for HP QC connection
     *
     * @param value like default: "qcConfig.json"
     */
    public ApollonConfiguration setHPQCConfig(String value) {
        PropertyResolver.setProperty(QC_CONFIG.key(), value);
        return this;
    }

    /**
     * set config file for TFS connection
     *
     * @param value like default: "tfsRunnerConfig.json"
     */
    public ApollonConfiguration setTFSRunnerConfig(String value) {
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
    public ApollonConfiguration setJiraConfig(String value) {
        PropertyResolver.setProperty(JIRA_CONFIG.key(), value);
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
    public ApollonConfiguration setIsOpenPDFInSystemReader(boolean value) {
        PropertyResolver.setProperty(OPEN_PDF_IN_SYSTEM_READER.key(), String.valueOf(value));
        return this;
    }

    /**
     * toggle if open pdf file in system reader after download
     *
     * @param value boolean, default false
     */
    public ApollonConfiguration setIsSimpleStringParameterAllowed(boolean value) {
        PropertyResolver.setProperty(SIMPLE_STRING_PARAMETER_ALLOWED.key(), String.valueOf(value));
        return this;
    }
}
