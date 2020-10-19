package ch.raiffeisen.testautomation.framework.configuration;

import ch.raiffeisen.testautomation.framework.common.IOUtils.FileLocator;
import ch.raiffeisen.testautomation.framework.common.IOUtils.FileOperation;
import ch.raiffeisen.testautomation.framework.common.enumerations.PropertyKey;
import org.apache.commons.lang3.EnumUtils;

import java.io.IOException;
import java.util.*;

import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.*;

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
        String pw = System.getProperty(PropertyKey.ENCODED_PASSWORD.key(), "");
        if (pw.isEmpty()) {
            log("INFO", "Please set your Base64 EncodedPassword as Runtime Parameter for the further execution");
            System.exit(-1);
        }
        return pw;
    }

    public static String getDefaultTestCaseLocation() {
        return System.getProperty(PropertyKey.DEFAULT_TESTCASE_LOCATION.key(), "testCases/");
    }

    public static String getDefaultTestDataLocation() {
        return System.getProperty(PropertyKey.DEFAULT_TESTDATA_LOCATION.key(), "testData/");
    }

    public static String getDefaultTestCaseReportLocation() {
        return System.getProperty(PropertyKey.DEFAULT_TESTCASE_REPORT_DIR.key(), "target/Reports/");
    }

    public static String getDefaultScreenshotFormat() {
        return System.getProperty(PropertyKey.DEFAULT_SCREENSHOT_FORMAT.key(), "PNG");
    }

    public static String getDefaultTestautomationPackage() {
        return System.getProperty(PropertyKey.DEFAULT_TEST_AUTOMATION_PACKAGE.key(), "ch.raiffeisen.testautomation");
    }

    public static String getCurrentTestCaseName() {
        return System.getProperty(PropertyKey.CURRENT_TESTCASE_NAME.key());
    }

    public static void setCurrentTestCaseName(String name) {
        setProperty(PropertyKey.CURRENT_TESTCASE_NAME.key(), name);
    }

    public static String getDefaultDataFormat() {
        return System.getProperty(PropertyKey.DEFAULT_DATE_FORMAT.key());
    }

    public static String getDefaultLoggerName() {
        return System.getProperty(PropertyKey.DEFAULT_LOGGER_NAME.key(), "SystemLogger");
    }

    public static String getAllureResultsDir() {
        return System.getProperty(PropertyKey.ALLURE_RESULTS_DIRECTORY.key(), "target/allure-results");
    }

    public static String getAllureReportDir() {
        return System.getProperty(PropertyKey.ALLURE_REPORT_DIRECTORY.key(), "target/allure-reports");
    }

    public static void setChromeDriverPath(String path) {
        setProperty(PropertyKey.WEBDRIVER_CHROME_DRIVER.key(), path);
    }

    public static String getChromeDriverPath() {
        return System.getProperty(PropertyKey.WEBDRIVER_CHROME_DRIVER.key());
    }

    public static void setWebDriverIEProperty(String path) {
        setProperty(PropertyKey.WEBDRIVER_IE_DRIVER.key(), path);
    }

    public static String getWebDriverName() {
        return System.getProperty(PropertyKey.WEBDRIVER_NAME.key(), "chrome");
    }

    public static void setWebDriverName(String driverName) {
        setProperty(PropertyKey.WEBDRIVER_NAME.key(), driverName);
    }

    public static String getDefaultWebDriverBinLocation() {
        return System.getProperty(PropertyKey.DEFAULT_WEBDRIVER_BIN_LOCATION.key(), "webDrivers/");
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getIEDriverFileName() {
        return System.getProperty(PropertyKey.WEBDRIVER_IE_FILENAME.key(), "IEDriverServer.exe");
    }

    public static void setIEDriverFileName(String fileName) {
        setProperty(PropertyKey.WEBDRIVER_IE_FILENAME.key(), fileName);
    }

    public static String getChromeDriverFileName() {
        return System.getProperty(PropertyKey.WEBDRIVER_CHROME_FILENAME.key(), "chromedriver");
    }

    public static void setChromeDriverFileName(String fileName) {
        setProperty(PropertyKey.WEBDRIVER_CHROME_FILENAME.key(), fileName);
    }

    public static String getReportViewType() {
        return System.getProperty(PropertyKey.ALLURE_REPORT_TYPE.key(), "default");
    }

    public static int getKeepLatestReport() {
        String count = System.getProperty(PropertyKey.ALLURE_REPORT_KEEP_LATEST.key(), "5");
        return Integer.parseInt(count);
    }

    public static int getExecutionThreads() {
        String count = System.getProperty(PropertyKey.DEFAULT_EXECUTION_THREADS.key(), "5");
        return Integer.parseInt(count);
    }

    public static String getSystemUser() {
        return System.getProperty("user.name");
    }

    public static String getSystemFileSeparator() {
        return System.getProperty("file.separator");
    }

    public static String getTextEditor() {
        return System.getProperty(PropertyKey.TEXT_EDITOR.key(), "notepad");
    }

    public static String getRemoteWebDriverConfig() {
        return System.getProperty(PropertyKey.TEST_DRIVER_REMOTE_CONFIG.key(), "remoteWebDriverConfig.json");
    }

    public static String getMobileAppDriverConfig() {
        return System.getProperty(PropertyKey.TEST_DRIVER_MOBILE_CONFIG.key());
    }

    public static boolean useHeadlessChrome() {
        return System.getProperty(PropertyKey.USE_HEADLESS_CHROME.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean demoModeEnabled() {
        return System.getProperty(PropertyKey.DEMO_MODE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean useMaximised() {
        return System.getProperty(PropertyKey.USE_FULLSCREEN.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean keepIECache() {
        return System.getProperty(PropertyKey.DRIVER_IE_KEEP_CACHE.key(), "false").equalsIgnoreCase("true");
    }

    public static String getDemoModeHighLightColor() {
        return System.getProperty(PropertyKey.DEMO_MODE_COLOR.key(), "green");
    }

    public static long getDemoModeSleep() {
        String value = System.getProperty(PropertyKey.DEMO_MODE_SLEEP.key(), "500");
        return Long.parseLong(value);
    }

    public static String getDefaultDriverConfigLocation() {
        return System.getProperty(PropertyKey.DEFAULT_DRIVER_CONFIG_LOCATION.key(), "driverConfig/");
    }

    public static String getDefaultPageConfigLocation() {
        return System.getProperty(PropertyKey.DEFAULT_PAGE_CONFIG_LOCATION.key(), "pageDefinitions/");
    }

    public static String getJiraCustomFieldConfig() {
        return System.getProperty(PropertyKey.JIRA_CUSTOMFIELD_CONFIG.key(), "driverConfig/jiraCustomFieldConfig.json");
    }

    public static boolean stopRunOnError() {
        return System.getProperty(PropertyKey.STOP_RUN_ON_ERROR.key(), "true").equalsIgnoreCase("true");
    }

    public static boolean getDefaultRunModeDebug() {
        return System.getProperty(PropertyKey.DEFAULT_RUN_MODE_DEBUG.key(), "false").equalsIgnoreCase("true");
    }

    public static String getTFSRunnerConfigFile() {
        return System.getProperty(PropertyKey.TFS_RUNNER_CONFIG.key(), "driverConfig/tfsRunnerConfig.json");
    }

    public static boolean isTFSFeedbackEnabled() {
        return System.getProperty(PropertyKey.DEFAULT_RUN_TFS_FEEDBACK.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRebaseAllureReport() {
        return System.getProperty(PropertyKey.ALLURE_REPORT_REBASE.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRetryEnabled() {
        return System.getProperty(PropertyKey.RETRY_MODE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isGenerateVideoEnabled() {
        return System.getProperty(PropertyKey.DEFAULT_GENERATE_VIDEO.key(), "false").equalsIgnoreCase("true");
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
        return System.getProperty(PropertyKey.DEFAULT_RESULTS_DB_STORE.key(), "false").equalsIgnoreCase("true");
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
        return Integer.parseInt(System.getProperty(PropertyKey.RETRY_OVER_STEPS.key(), "1"));
    }

    public static int getDefaultVideoFPS() {
        return Integer.parseInt(System.getProperty(PropertyKey.DEFAULT_VIDEO_FPS.key(), "1"));
    }

    public static String getQCConfigFile() {
        return System.getProperty(PropertyKey.TEST_QC_CONFIG.key(), "driverConfig/qcConfig.json");
    }

    public static String getDBConfigFile() {
        return System.getProperty(PropertyKey.TEST_DB_CONFIG.key(), "driverConfig/dbConfig.json");
    }

    public static String getJIRAConfigFile() {
        return System.getProperty(PropertyKey.TEST_JIRA_CONFIG.key(), "driverConfig/jiraConfig.json");
    }

    public static String getRESTConfigFile() {
        return System.getProperty(PropertyKey.TEST_REST_CONFIG.key(), "driverConfig/restConfig.json");
    }

    public static String getDefaultVideoFormat() {
        return System.getProperty(PropertyKey.DEFAULT_VIDEO_FORMAT.key(), "mp4");
    }

    public static boolean isMultiThreadingEnabled() {
        return System.getProperty(PropertyKey.DEFAULT_EXECUTION_MULTI_THREADING.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isObjectIdEnabled() {
        return System.getProperty(PropertyKey.RUNNER_MARKUP_OBJECTID_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isSeleniumHubEnabled() {
        return System.getProperty(PropertyKey.EXECUTION_REMOTE_SELENIUM_HUB.key(), "false").equalsIgnoreCase("true");
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

    public static String getRemoteWebDriverFolder() {
        return System.getProperty(PropertyKey.REMOTE_WEB_DRIVER_FOLDER.key());
    }

    public static String getTessDataLocation() {
        return System.getProperty(PropertyKey.DEFAULT_OCR_TESSDATA_LOCATION.key(), "tessdata");
    }

    public static String getTFSConfigurationID() {
        return System.getProperty(PropertyKey.TFS_CONFIGURATION_ID.key(), null);
    }

    public static String getTestEnvironment() {
        return System.getProperty(PropertyKey.TEST_ENVIRONMENT.key(), "");
    }

    public static String getTestDataFolder() {
        String folder = PropertyResolver.getDefaultTestDataLocation() + PropertyResolver.getTestEnvironment();
        if (FileLocator.findLocalResource(folder) == null) {
            folder = PropertyResolver.getDefaultTestDataLocation();
        }
        return folder;
    }
}
