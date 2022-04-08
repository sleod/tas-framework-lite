package ch.qa.testautomation.framework.configuration;

import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.PropertyKey;
import org.apache.commons.lang3.EnumUtils;

import java.io.IOException;
import java.util.*;

import static ch.qa.testautomation.framework.common.enumerations.PropertyKey.*;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.*;

public class PropertyResolver {
    private final static Properties properties = new Properties();

    public static boolean loadTestRunProperties(String filePath) {
        boolean isDefaultPath = false;
        try {
            if (FileOperation.retrieveFileFromResourcesAsStream(filePath) == null) {
                isDefaultPath = true;
                properties.load(FileOperation.retrieveFileFromResourcesAsStream("properties/DefaultTestRunProperties.properties"));
            } else {
                properties.load(FileOperation.retrieveFileFromResourcesAsStream(filePath));
            }
            if (properties.isEmpty()) {
                error(new RuntimeException("Properties is empty!! Test Run interrupted!"));
                System.exit(-1);
            }
            properties.forEach((key, value) -> {
                setProperty(String.valueOf(key), String.valueOf(value));
            });
        } catch (IOException e) {
            error(e);
        }
        return isDefaultPath;
    }

    public static String decodeBase64(String encoded) {
        byte[] context = Base64.getDecoder().decode(encoded);
        return new String(context);
    }

    public static String encodeBase64(String freeText) {
        byte[] context = Base64.getEncoder().encode(freeText.getBytes());
        return new String(context);
    }

    public static String getPasswordFromProperty() {
        String pw = System.getProperty(ENCODED_PASSWORD.key(), "");
        if (pw.isEmpty()) {
            log("INFO", "Please set your Base64 EncodedPassword as Runtime Parameter for the further execution");
            System.exit(-1);
        }
        return pw;
    }

    public static String getDefaultTestCaseLocation() {
        return System.getProperty(DEFAULT_TESTCASE_LOCATION.key(), "testCases/");
    }

    public static String getDefaultTestDataLocation() {
        return System.getProperty(DEFAULT_TESTDATA_LOCATION.key(), "testData/");
    }

    public static String getDefaultTestCaseReportLocation() {
        return System.getProperty(DEFAULT_TESTCASE_REPORT_DIR.key(), "target/Reports/");
    }

    public static String getDefaultScreenshotFormat() {
        return System.getProperty(DEFAULT_SCREENSHOT_FORMAT.key(), "PNG");
    }

    public static String getDefaultTestautomationPackage() {
        return System.getProperty(DEFAULT_TEST_AUTOMATION_PACKAGE.key(), "ch.raiffeisen.testautomation");
    }

    public static String getCurrentTestCaseName() {
        return System.getProperty(CURRENT_TESTCASE_NAME.key());
    }

    public static void setCurrentTestCaseName(String name) {
        setProperty(CURRENT_TESTCASE_NAME.key(), name);
    }

    public static String getDefaultDataFormat() {
        return System.getProperty(DEFAULT_DATE_FORMAT.key());
    }

    public static String getDefaultLoggerName() {
        return System.getProperty(DEFAULT_LOGGER_NAME.key(), "SystemLogger");
    }

    public static boolean isAllureReportService() {
        return System.getProperty(ALLURE_REPORT_SERVICE.key(), "false").equalsIgnoreCase("true");
    }

    public static String getAllureResultsDir() {
        return System.getProperty(ALLURE_RESULTS_DIRECTORY.key(), "target/allure-results/");
    }

    public static String getAllureReportDir() {
        return System.getProperty(ALLURE_REPORT_DIRECTORY.key(), "target/allure-reports/");
    }

    public static void setChromeDriverPath(String path) {
        setProperty(WEBDRIVER_CHROME_DRIVER.key(), path);
    }

    public static String getChromeDriverPath() {
        return System.getProperty(WEBDRIVER_CHROME_DRIVER.key());
    }

    public static void setWebDriverEdgeProperty(String path) {
        setProperty(WEBDRIVER_EDGE_DRIVER.key(), path);
    }

    public static String getWebDriverName() {
        return System.getProperty(WEBDRIVER_NAME.key(), "chrome");
    }

    public static void setWebDriverName(String driverName) {
        setProperty(WEBDRIVER_NAME.key(), driverName);
    }

    public static String getDefaultWebDriverBinLocation() {
        return System.getProperty(DEFAULT_WEBDRIVER_BIN_LOCATION.key(), "webDrivers/");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getEdgeDriverFileName() {
        return System.getProperty(WEBDRIVER_EDGE_FILENAME.key(), "msedgedriver");
    }

    public static void setEdgeDriverFileName(String fileName) {
        setProperty(WEBDRIVER_EDGE_FILENAME.key(), fileName);
    }

    public static String getChromeDriverFileName() {
        return System.getProperty(WEBDRIVER_CHROME_FILENAME.key(), "chromedriver");
    }

    public static void setChromeDriverFileName(String fileName) {
        setProperty(WEBDRIVER_CHROME_FILENAME.key(), fileName);
    }

    public static String getReportViewType() {
        return System.getProperty(ALLURE_REPORT_TYPE.key(), "default");
    }

    public static int getKeepLatestReport() {
        String count = System.getProperty(ALLURE_REPORT_KEEP_LATEST.key(), "5");
        return Integer.parseInt(count);
    }

    public static int getExecutionThreads() {
        String count = System.getProperty(DEFAULT_EXECUTION_THREADS.key(), "5");
        return Integer.parseInt(count);
    }

    public static boolean isRestartDriverAfterExecution() {
        return System.getProperty(DEFAULT_EXECUTION_DRIVER_RESTART.key(), "true").equalsIgnoreCase("true");
    }

    public static String getSystemUser() {
        return System.getProperty("user.name");
    }

    public static String getSystemFileSeparator() {
        return System.getProperty("file.separator");
    }

    public static String getTextEditor() {
        return System.getProperty(TEXT_EDITOR.key(), "notepad");
    }

    public static String getRemoteWebDriverConfig() {
        return System.getProperty(TEST_DRIVER_REMOTE_CONFIG.key(), "remoteWebDriverConfig.json");
    }

    public static String getMobileAppDriverConfig() {
        return System.getProperty(TEST_DRIVER_MOBILE_CONFIG.key());
    }

    public static boolean useHeadlessMode() {
        //exist @NonHeadless method then return false
        if (System.getProperty(METHOD_NONHEADLESS_EXISTS.key(), "false").equalsIgnoreCase("true")) {
            return false;
        } else {
            return System.getProperty(USE_HEADLESS_CHROME.key(), "false").equalsIgnoreCase("true");
        }
    }

    public static boolean demoModeEnabled() {
        return System.getProperty(DEMO_MODE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean useMaximised() {
        return System.getProperty(USE_FULLSCREEN.key(), "false").equalsIgnoreCase("true");
    }

    public static String getScreenSize() {
        return System.getProperty(SCREEN_SIZE.key(), "1920,1080");
    }

    public static String getDemoModeHighLightColor() {
        return System.getProperty(DEMO_MODE_COLOR.key(), "green");
    }

    public static long getDemoModeSleep() {
        String value = System.getProperty(DEMO_MODE_SLEEP.key(), "500");
        return Long.parseLong(value);
    }

    public static String getDefaultDriverConfigLocation() {
        return System.getProperty(DEFAULT_DRIVER_CONFIG_LOCATION.key(), "driverConfig/");
    }

    public static String getDefaultPageConfigLocation() {
        return System.getProperty(DEFAULT_PAGE_CONFIG_LOCATION.key(), "pageDefinitions/");
    }

    public static String getJiraConfigFile() {
        String filename = System.getProperty(JIRA_CONFIG.key(), "jiraConfig.json");
        return getDefaultDriverConfigLocation() + filename;
    }

    public static String getJiraExecConfigFile() {
        String filename = System.getProperty(JIRA_EXEC_CONFIG.key(), "jiraExecutionConfig.json");
        return getDefaultDriverConfigLocation() + filename;
    }

    public static boolean stopRunOnError() {
        return System.getProperty(STOP_RUN_ON_ERROR.key(), "true").equalsIgnoreCase("true");
    }

    public static boolean getDefaultRunModeDebug() {
        return System.getProperty(DEFAULT_RUN_MODE_DEBUG.key(), "false").equalsIgnoreCase("true");
    }

    public static String getTFSRunnerConfigFile() {
        String filename = System.getProperty(TFS_RUNNER_CONFIG.key(), "tfsRunnerConfig.json");
        return getDefaultDriverConfigLocation() + filename;
    }

    public static String getReportServiceRunnerConfigFile() {
        String filename = System.getProperty(REPORT_SERVICE_RUNNER_CONFIG.key(), "reportServiceRunnerConfig.json");
        return getDefaultDriverConfigLocation() + filename;
    }

    public static boolean isTFSConnectEnabled() {
        return System.getProperty(DEFAULT_RUN_TFS_CONNECT.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isTFSSyncEnabled() {
        return System.getProperty(DEFAULT_RUN_TFS_SYNC.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isJIRAConnectEnabled() {
        return System.getProperty(DEFAULT_RUN_JIRA_CONNECT.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isJIRASyncEnabled() {
        return System.getProperty(DEFAULT_RUN_JIRA_SYNC.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRebaseAllureReport() {
        return System.getProperty(ALLURE_REPORT_REBASE.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean showAllEnvironmentVariables() {
        return System.getProperty(ALLURE_REPORT_ALL_ENVIRONMENT_VARIABLES.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRetryEnabled() {
        return System.getProperty(RETRY_MODE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isGenerateVideoEnabled() {
        return System.getProperty(DEFAULT_GENERATE_VIDEO.key(), "false").equalsIgnoreCase("true");
    }

    public static List<String> getAllPropertiesWith(String subKey) {
        List<String> settings = new LinkedList<>();
        for (String name : EnumUtils.getEnumMap(PropertyKey.class).keySet()) {
            if (name.toLowerCase().contains(subKey.toLowerCase())) {
                String key = PropertyKey.valueOf(name).key();
                String setting = System.getProperty(key);
                if (setting == null) {
                    warn("Property for key: " + key + " was not set!!");
                }
                settings.add(setting);
            }
        }
        return settings;
    }

    public static boolean isStoreResultsToDBEnabled() {
        return System.getProperty(DEFAULT_RESULTS_DB_STORE.key(), "false").equalsIgnoreCase("true");
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value.trim());
        System.setProperty(key, value.trim());
        trace(key + " -> " + value.trim());
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getRetryOverSteps() {
        return Integer.parseInt(System.getProperty(RETRY_OVER_STEPS.key(), "1"));
    }

    public static int getDefaultVideoFPS() {
        return Integer.parseInt(System.getProperty(DEFAULT_VIDEO_FPS.key(), "1"));
    }

    public static String getQCConfigFile() {
        String filename = System.getProperty(QC_CONFIG.key(), "qcConfig.json");
        return getDefaultDriverConfigLocation() + filename;
    }

    public static String getDBConfigFile() {
        String filename = System.getProperty(DB_CONFIG.key(), "dbConfig.json");
        return getDefaultDriverConfigLocation() + filename;
    }

    public static String getRESTConfigFile() {
        String filename = System.getProperty(REST_CONFIG.key(), "restConfig.json");
        return getDefaultDriverConfigLocation() + filename;
    }

    public static String getDefaultVideoFormat() {
        return System.getProperty(DEFAULT_VIDEO_FORMAT.key(), "mp4");
    }

    public static boolean isMultiThreadingEnabled() {
        return System.getProperty(DEFAULT_EXECUTION_MULTI_THREADING.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isObjectIdEnabled() {
        return System.getProperty(RUNNER_MARKUP_OBJECTID_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isSeleniumHubEnabled() {
        return System.getProperty(EXECUTION_REMOTE_SELENIUM_HUB.key(), "false").equalsIgnoreCase("true");
    }

    public static void setProperties(Map<String, String> properties) {
        properties.forEach(PropertyResolver::setProperty);
    }

    public static void setStopOnErrorPerStep(boolean stop) {
        System.setProperty("StopOnErrorPerStep", String.valueOf(stop));
    }

    public static boolean getStopOnErrorPerStep() {
        return System.getProperty("StopOnErrorPerStep", "true").equalsIgnoreCase("true");
    }

    public static boolean isLinux() {
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static String getDriverResourceLocation() {
        String location = System.getProperty(RESOURCE_DRIVER_LOCATION.key(), "");
        if (location.isEmpty()) {
            location = System.getProperty(REMOTE_WEB_DRIVER_FOLDER.key());
            warn("Driver Resource Location setting in Properties with key: 'remote.web.driver.folder' is deprecated!" +
                    "\n Please change to 'resource.driver.location' and add 'resource.project=ap.testtools' additionally. ");
        }
        if (location.isEmpty()) {
            warn("Driver Resource Location is not defined! Please add 'resource.driver.location=<location>' to Properties!");
        }
        return location;
    }

    public static String getResourceTFSProject() {
        return System.getProperty(RESOURCE_PROJECT.key(), "ap.testtools");
    }

    public static String getTessDataLocation() {
        return System.getProperty(DEFAULT_OCR_TESSDATA_LOCATION.key(), "tessdata");
    }

    public static String getTFSConfigurationID() {
        return System.getProperty(TFS_CONFIGURATION_ID.key(), null);
    }

    public static String getTestEnvironment() {
        return System.getProperty(TEST_ENVIRONMENT.key(), "");
    }

    public static String getTestDataFolder() {
        String folder = PropertyResolver.getDefaultTestDataLocation() + PropertyResolver.getTestEnvironment();
        if (FileLocator.findLocalResource(folder) == null) {
            folder = PropertyResolver.getDefaultTestDataLocation();
        }
        return folder;
    }

    public static String getDefaultDownloadDir() {
        return System.getProperty(DEFAULT_DOWNLOAD_LOCATION.key(), "");
    }

    public static int getSelenideTimeout() {
        String timeout = System.getProperty(SELENIDE_CONFIGURATION_TIMEOUT.key(), "5");
        return Integer.parseInt(timeout) * 1000;
    }

    public static String getRuntimeDBUser() {
        return System.getProperty(RUNTIME_DB_USER.key(), "");
    }

    public static String getRuntimeDBHost() {
        return System.getProperty(RUNTIME_DB_HOST.key(), "");
    }

    public static String getRuntimeDBPassword() {
        return System.getProperty(RUNTIME_DB_PASSWORD.key(), "");
    }

    public static String getRuntimeDBPort() {
        return System.getProperty(RUNTIME_DB_PORT.key(), "");
    }

    public static String getRuntimeDBType() {
        return System.getProperty(RUNTIME_DB_TYPE.key(), "");
    }

    public static String getRuntimeDBSName() {
        return System.getProperty(RUNTIME_DB_SNAME.key(), "");
    }

    public static String getRuntimeRestUser() {
        return System.getProperty(RUNTIME_REST_USER.key(), "");
    }

    public static String getRuntimeRestPassword() {
        return System.getProperty(RUNTIME_REST_PASSWORD.key(), "");
    }

    public static String getRuntimeRestPAT() {
        return System.getProperty(RUNTIME_REST_PAT.key(), "");
    }

    public static String getRuntimeRestHost() {
        return System.getProperty(RUNTIME_REST_HOST.key(), "");
    }
}
