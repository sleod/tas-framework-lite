package io.github.sleod.tas.configuration;

import io.github.sleod.tas.common.IOUtils.FileLocator;
import io.github.sleod.tas.common.enumerations.DownloadStrategy;
import io.github.sleod.tas.common.enumerations.WebDriverName;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

import static io.github.sleod.tas.common.enumerations.PropertyKey.*;
import static io.github.sleod.tas.common.logging.SystemLogger.*;
import static io.github.sleod.tas.common.utils.StringTextUtils.isValid;

/**
 * Utility class for resolving properties.
 */
public class PropertyResolver {
    private final static ThreadLocal<Properties> propertyThreadsMap = new ThreadLocal<>();


    /**
     * Sets a property value for the current thread.
     *
     * @param key   the property key
     * @param value the property value
     */
    public static void setProperty(String key, String value) {
        if (isValid(value)) {
            geCurrentProperties().setProperty(key, value.trim());
            if (!key.toLowerCase()
                    .contains("pat") && !key.toLowerCase()
                    .contains("password")) {
                info("Set " + key + " -> " + value);
            } else {
                info("Set " + key + " -> " + value.substring(0, 2) + "******" + value.substring(value.length() - 2));
            }
        } else {
            debug("Try to set invalid value with Property Key: " + key);
        }
    }


    /**
     * Gets the current thread's Properties object, creating it if necessary.
     *
     * @return the Properties object for the current thread
     */
    public static Properties geCurrentProperties() {
        if (propertyThreadsMap.get() == null) {
            propertyThreadsMap.set(new Properties());
        }
        return propertyThreadsMap.get();
    }


    /**
     * Decodes a Base64-encoded string.
     *
     * @param encoded the Base64-encoded string
     * @return the decoded string, or the original if not valid Base64
     */
    public static String decodeBase64(String encoded) {
        if (isValid(encoded)) {
            if (org.apache.commons.codec.binary.Base64.isBase64(encoded.getBytes())) {
                byte[] context = Base64.getDecoder()
                        .decode(encoded);
                return new String(context);
            } else {
                debug("Given String is not suitable for base64 decode! Original String will be used!");
                return encoded;
            }
        } else {
            debug("Given String is not valid! Original String will be used!");
            return encoded;
        }
    }


    /**
     * Encodes a string to Base64.
     *
     * @param freeText the string to encode
     * @return the Base64-encoded string
     */
    public static String encodeBase64(String freeText) {
        byte[] context = Base64.getEncoder().encode(freeText.getBytes());
        return new String(context);
    }

    public static String getTestCaseLocation() {
        return getProperty(TESTCASE_LOCATION.key(), "testCases/");
    }

    public static boolean isCleanUpResults() {
        return getProperty(ALLURE_REPORT_CLEANUP.key(), "true").equalsIgnoreCase("true");
    }

    public static String getTestDataLocation() {
        return getProperty(TESTDATA_LOCATION.key(), "testData/");
    }


    public static String getTestCaseReportLocation() {
        return getProperty(TESTCASE_REPORT_DIR.key(), "target/Reports/");
    }

    public static String getTestCaseFileExtension() {
        return getProperty(TESTCASE_FILE_EXTENSION.key(), ".tas");
    }

    public static List<String> getMetaFilter() {
        String metaString = getProperty(RUN_META_FILTER.key(), "");
        return metaString.isEmpty() ? Collections.emptyList() : Arrays.stream(metaString.split(","))
                .map(String::trim)
                .toList();
    }

    /**
     * get screenshot format property.
     *
     * @return the screenshot format
     */
    public static String getScreenshotFormat() {
        return getProperty(SCREENSHOT_FORMAT.key(), "png");
    }

    /**
     * Get the test automation package property.
     *
     * @return the test automation package
     */
    public static String getTestautomationPackage() {
        return getProperty(TEST_AUTOMATION_PACKAGE.key(), "ch.qa.testautomation");
    }

    public static String getCurrentTestCaseName() {
        return getProperty(CURRENT_TESTCASE_NAME.key(), "");
    }


    /**
     * set current test case name property.
     */
    public static void setCurrentTestCaseName(String name) {
        setProperty(CURRENT_TESTCASE_NAME.key(), name);
    }

    /**
     * get date format property.
     *
     * @return date format
     */
    public static String getDateFormat() {
        return getProperty(DATE_FORMAT.key(), "yyyy-MM-dd");
    }

    public static boolean isAllureReportServiceEnabled() {
        return getProperty(ALLURE_REPORT_SERVICE.key(), "false").equalsIgnoreCase("true");
    }


    /**
     * get allure results directory property.
     *
     * @return allure results directory
     */
    public static String getAllureResultsDirectory() {
        return getProperty(ALLURE_RESULTS_LOCATION.key(), "target/allure-results/");
    }

    /**
     * get allure report directory property.
     *
     * @return allure report directory
     */
    public static String getAllureReportDirectory() {
        return getProperty(ALLURE_REPORT_LOCATION.key(), "target/allure-reports/");
    }

    public static void setChromeDriverPath(String path) {
        System.setProperty(WEBDRIVER_CHROME_DRIVER.key(), path);
    }

    /**
     * get chrome driver path property.
     *
     * @return chrome driver path
     */
    public static String getChromeDriverPath() {
        return getProperty(WEBDRIVER_CHROME_DRIVER.key(), "");
    }

    /**
     * set edge driver path property.
     * @param path the edge driver path
     */
    public static void setEdgeDriverPath(String path) {
        System.setProperty(WEBDRIVER_EDGE_DRIVER.key(), path);
    }

    public static String getEdgeDriverPath() {
        return getProperty(WEBDRIVER_EDGE_DRIVER.key(), "");
    }

    /**
     * get browser profile directory property.
     *
     * @return browser profile directory
     */
    public static String getWebDriverName() {
        return getProperty(WEB_DRIVER_NAME.key(), WebDriverName.CHROME.getName());
    }

    /**
     * get browser name
     *
     * @return browser name
     */
    public static String getUsedBrowserName() {
        return getProperty(USE_BROWSER_NAME.key(), WebDriverName.CHROME.getName());
    }

    public static String getWebDriverBinLocation() {
        return getProperty(WEBDRIVER_BIN_LOCATION.key(), "webDrivers/");
    }

    public static boolean isWindows() {
        return getProperty("os.name").toLowerCase().contains("windows");
    }

    public static String getEdgeDriverFileName() {
        return getProperty(WEBDRIVER_EDGE_FILENAME.key(), "msedgedriver");
    }

    public static void setEdgeDriverFileName(String fileName) {
        setProperty(WEBDRIVER_EDGE_FILENAME.key(), fileName);
    }

    public static void setBrowserVersion(String browserVersion) {
        setProperty(DRIVER_BROWSER_VERSION.key(), browserVersion);
    }

    public static String getBrowserVersion() {
        return getProperty(DRIVER_BROWSER_VERSION.key(), "");
    }

    public static String getChromeDriverFileName() {
        return getProperty(WEBDRIVER_CHROME_FILENAME.key(), "chromedriver");
    }

    public static void setChromeDriverFileName(String fileName) {
        setProperty(WEBDRIVER_CHROME_FILENAME.key(), fileName);
    }

    public static boolean isRestartDriverAfterExecutionEnabled() {
        return getProperty(RUN_DRIVER_RESTART.key(), "true").equalsIgnoreCase("true");
    }

    public static boolean isKeepBrowserOnErrorEnabled() {
        return getProperty(DEBUG_KEEP_BROWSER.key(), "false").equalsIgnoreCase("true");
    }

    public static void setKeepBrowserOnErrorEnabled(boolean isKeep) {
        setProperty(DEBUG_KEEP_BROWSER.key(), String.valueOf(isKeep));
    }

    public static String getSystemUser() {
        return getProperty("user.name");
    }

    public static String getSystemFileSeparator() {
        return getProperty("file.separator");
    }

    /**
     * get text editor property.
     *
     * @return text editor name
     */
    public static String getTextEditor() {
        return getProperty(TEXT_EDITOR.key(), "notepad");
    }

    /**
     * gets the remote WebDriver config property.
     *
     * @return the remote WebDriver config
     */
    public static String getRemoteWebDriverConfig() {
        return getProperty(TEST_DRIVER_REMOTE_CONFIG.key(), "");
    }

    /**
     * Gets the mobile app driver config property.
     *
     * @return the mobile app driver config
     */
    public static String getMobileAppDriverConfig() {
        return getProperty(TEST_DRIVER_MOBILE_CONFIG.key(), "");
    }

    /**
     * Get if headless mode is enabled, default is true.
     *
     * @return true if headless mode is enabled, false otherwise
     */
    public static boolean isHeadlessModeEnabled() {
        //exist @NonHeadless method then return false
        if (getProperty(METHOD_NONHEADLESS_EXISTS.key(), "false").equalsIgnoreCase("true")) {
            return false;
        } else {
            return getProperty(DRIVER_BROWSER_HEADLESS.key(), "true").equalsIgnoreCase("true");
        }
    }

    /**
     * Checks if demo mode is enabled.
     *
     * @return true if demo mode is enabled, false otherwise
     */
    public static boolean isDemoModeEnabled() {
        return getProperty(DEMO_MODE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    /**
     * checks if browser fullscreen mode is enabled.
     *
     * @return if browser fullscreen mode is enabled
     */
    public static boolean getBrowserFullscreenEnabled() {
        return getProperty(DRIVER_BROWSER_FULLSCREEN.key(), "false").equalsIgnoreCase("true");
    }

    /**
     * Gets the browser screen size property.
     *
     * @return the browser screen size
     */
    public static String getBrowserScreenSize() {
        return getProperty(BROWSER_SCREEN_SIZE.key(), "1920,1080");
    }

    /**
     * Gets the browser screen width property.
     *
     * @return the browser screen width
     */
    public static int getBrowserScreenWidth() {
        return getBrowserScreen(0);
    }

    /**
     * Gets the browser screen high property.
     *
     * @return the browser screen high
     */
    public static int getBrowserScreenHigh() {
        return getBrowserScreen(1);
    }

    /**
     * Gets the browser screen size property.
     *
     * @return the browser screen size
     */
    private static int getBrowserScreen(int i) {
        String screen = getBrowserScreenSize();
        if (getBrowserFullscreenEnabled()) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            return List.of(screenSize.getWidth(), screenSize.getHeight()).get(i).intValue();
        }
        String[] screens = screen.split(",");
        return Integer.parseInt(screens[i].trim());
    }

    /**
     * Gets the Driver Configs location dir.
     *
     * @return Driver Configs location dir.
     */
    public static String getDriverConfigLocation() {
        return getProperty(DRIVER_CONFIG_LOCATION.key(), "driverConfig/");
    }

    public static String getMobileDriverConfigLocation() {
        return getDriverConfigLocation() + "mobile/";
    }

    public static String getPageConfigLocation() {
        return getProperty(PAGE_CONFIG_LOCATION.key(), "pageDefinitions/");
    }

    public static String getJiraConfigFile() {
        String filename = getProperty(JIRA_CONFIG.key(), "jiraConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getJiraExecutionConfigFile() {
        String filename = getProperty(JIRA_EXEC_CONFIG.key(), "jiraExecutionConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static boolean isStopOnErrorEnabled() {
        return getProperty(RUN_STOP_ON_ERROR.key(), "true").equalsIgnoreCase("true");
    }

    public static String getDownloadStrategy() {
        return getProperty(DRIVER_DOWNLOAD_STRATEGY.key(), DownloadStrategy.AUTO.name());
    }

    public static String getTASLogLevel() {
        return getProperty(LOG_LEVEL_TAS.key(), Level.INFO.name());
    }

    public static String getTFSRunnerConfigFile() {
        String filename = getProperty(TFS_RUNNER_CONFIG.key(), "tfsRunnerConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getReportServiceRunnerConfigFile() {
        String filename = getProperty(REPORT_SERVICE_RUNNER_CONFIG.key(), "reportServiceRunnerConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static boolean isTFSSyncEnabled() {
        return getProperty(RUN_TFS_SYNC.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isJIRASyncEnabled() {
        return getProperty(RUN_JIRA_SYNC.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isSyncToQCEnabled() {
        return getProperty(RUN_QC_SYNC.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRebaseAllureReportEnabled() {
        return getProperty(ALLURE_REPORT_REBASE.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRetryOnErrorEnabled() {
        return getProperty(RETRY_MODE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isGenerateVideoEnabled() {
        return getProperty(GENERATE_VIDEO.key(), "false").equalsIgnoreCase("true");
    }

    public static String getProperty(String key) {
        return System.getProperties()
                .containsKey(key) ? System.getProperty(key) : geCurrentProperties().getProperty(key);
    }

    private static String getProperty(String key, String defValue) {
        if (Objects.isNull(getProperty(key))) {
            setProperty(key, defValue);
            return defValue;
        }
        return getProperty(key) == null ? defValue : getProperty(key);
    }

    public static int getRetryOverSteps() {
        return Integer.parseInt(getProperty(RETRY_OVER_STEPS.key(), "1"));
    }

    public static String getQCConfigFile() {
        String filename = getProperty(QC_CONFIG.key(), "qcConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getDBConfigFile() {
        String filename = getProperty(DB_CONFIG.key(), "dbConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getRestConfigFile() {
        String filename = getProperty(REST_CONFIG.key(), "restConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getDriverDownloadConfigFile() {
        String filename = getProperty(DRIVER_DOWNLOAD_CONFIG.key(), "driverDownloadConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getVideoFormat() {
        return getProperty(VIDEO_FORMAT.key(), "mp4");
    }

    public static String getTestIdAttribute() {
        return getProperty(TEST_ID_ATTRIBUTE.key(), "id");
    }

    public static void setProperties(Map<String, String> properties) {
        properties.forEach(PropertyResolver::setProperty);
    }

    public static boolean isCDPEnabled() {
        return getProperty(CDP_ALLOWED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isLinux() {
        return getProperty("os.name").toLowerCase()
                .contains("linux");
    }

    public static boolean isMac() {
        return getProperty("os.name").toLowerCase()
                .contains("mac");
    }

    public static String getResourceTFSProject() {
        return getProperty(RESOURCE_PROJECT.key(), "ap.testtools");
    }

    public static String getTessDataLocation() {
        return getProperty(OCR_TESSDATA_LOCATION.key(), "tessdata");
    }

    public static String getTFSConfigurationID() {
        return getProperty(TFS_CONFIGURATION_ID.key(), null);
    }

    public static String getTestEnvironment() {
        return getProperty(TEST_ENVIRONMENT.key(), "");
    }

    public static String getTestDataFolder() {
        String folder = PropertyResolver.getTestDataLocation() + PropertyResolver.getTestEnvironment();
        if (FileLocator.findLocalResource(folder) == null) {
            folder = PropertyResolver.getTestDataLocation();
        }
        return folder;
    }

    public static String getDownloadDir() {
        String dir = System.getProperty("user.home") + File.separator + "Downloads" + File.separator;
        return getProperty(DOWNLOAD_LOCATION.key(), dir);
    }

    public static boolean isSkipPlaywrightBrowserDownload() {
        return getProperty(PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD.key(), "true").equalsIgnoreCase("true");
    }

    public static String getNodeJSBinFilePath() {
        return getProperty(PLAYWRIGHT_NODEJS_PATH.key(), "");
    }

    public static String getBrowserBinPath() {
        return getProperty(BROWSER_BIN_PATH.key(), "");
    }

    public static void setBrowserBinPath(String binPath) {
        setProperty(BROWSER_BIN_PATH.key(), binPath);
    }

    public static int getDriverWaitTimeout() {
        String timeout = getProperty(DRIVER_WAIT_TIMEOUT.key(), "10");
        return Integer.parseInt(timeout) * 1000;
    }

    public static boolean isExecutionRemoteParallelEnabled() {
        return getProperty(EXECUTION_REMOTE_PARALLEL.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRemoteDeviceEnabled() {
        return getProperty(EXECUTION_REMOTE_DEVICE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static void setRemoteDeviceEnabled(boolean value) {
        PropertyResolver.setProperty(EXECUTION_REMOTE_DEVICE_ENABLED.key(), String.valueOf(value));
    }

    public static String getStartUrl() {
        return getProperty(RUN_START_URL.key());
    }

    public static String getJiraUser() {
        return getProperty(JIRA_USER.key(), "");
    }

    public static String getJiraPassword() {
        return getProperty(JIRA_PASSWORD.key(), "");
    }

    public static String getJiraPAT() {
        return getProperty(JIRA_PAT.key(), "");
    }

    public static String getJiraHost() {
        return getProperty(JIRA_HOST.key(), "");
    }

    public static String getJiraProxyHost() {
        return getProperty(JIRA_PROXY_HOST.key(), "");
    }

    public static int getJiraProxyPort() {
        String port = getProperty(JIRA_PROXY_PORT.key(), "");
        return port.isEmpty() ? 0 : Integer.parseInt(port);
    }

    public static String getRestUser() {
        return getProperty(REST_USER.key(), "");
    }

    public static String getRestPassword() {
        return getProperty(REST_PASSWORD.key(), "");
    }

    public static String getRestPAT() {
        return getProperty(REST_PAT.key(), "");
    }

    public static String getRestHost() {
        return getProperty(REST_HOST.key(), "");
    }

    public static String getDBHost() {
        return getProperty(DB_HOST.key(), "");
    }

    public static String getDBUser() {
        return getProperty(DB_USER.key(), "");
    }

    public static String getDBPassword() {
        return getProperty(DB_PASSWORD.key(), "");
    }

    public static String getQCUser() {
        return getProperty(QC_USER.key());
    }

    public static String getQCPassword() {
        return getProperty(QC_PASSWORD.key());
    }

    public static String getDBType() {
        return getProperty(DB_TYPE.key(), "");
    }

    public static String getDBName() {
        return getProperty(DB_NAME.key(), "");
    }

    public static String getDBPort() {
        return getProperty(DB_PORT.key(), "");
    }

    public static void setNonHeadlessMethodExists(String booleanString) {
        setProperty(METHOD_NONHEADLESS_EXISTS.key(), booleanString);
    }

    public static int getRemoteExecutionMaxThreads() {
        String max = getProperty(EXECUTION_REMOTE_THREAD_MAX.key(), "5");
        return Integer.parseInt(max);
    }

    public static boolean hasNonHeadlessMethod() {
        return getProperty(METHOD_NONHEADLESS_EXISTS.key(), "false").equalsIgnoreCase("true");
    }

    public static String getBrowserProfileDir() {
        return getProperty(BROWSER_PROFILE_DIR.key(), "target/generated-user-data/chrome-profile");
    }

    public static boolean isOpenPDFInSystemReader() {
        return getProperty(OPEN_PDF_IN_SYSTEM_READER.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isSimpleStringParameterAllowed() {
        return getProperty(SIMPLE_STRING_PARAMETER_ALLOWED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isGenerateSingleFileReport() {
        return getProperty(GENERATE_SINGLE_FILE_REPORT.key(), "false").equalsIgnoreCase("true");
    }

    public static void healthCheck() {
        info("Check setting conflict ... ");
        boolean isOK = true;
        if (isHeadlessModeEnabled() && isKeepBrowserOnErrorEnabled()) {
            error(DRIVER_BROWSER_HEADLESS.key() + " and " + DEBUG_KEEP_BROWSER.key() + " can not be 'true' at same time!");
            isOK = false;
        }

        if (isKeepBrowserOnErrorEnabled() && !isStopOnErrorEnabled()) {
            error(DEBUG_KEEP_BROWSER.key() + "=true make sense only wenn " + RUN_STOP_ON_ERROR.key() + "=true!");
            isOK = false;
        }

        if (!isRestartDriverAfterExecutionEnabled() && isKeepBrowserOnErrorEnabled()) {
            info(DEBUG_KEEP_BROWSER + "=true and " + RUN_DRIVER_RESTART + "=false will force a Driver Restart after Execution!");
        }

        if (isRetryOnErrorEnabled() && !isStopOnErrorEnabled()) {
            error(RETRY_MODE_ENABLED + "=true required the " + RUN_STOP_ON_ERROR + " also be true!");
            isOK = false;
        }

        if (isGenerateVideoEnabled() && isDemoModeEnabled()) {
            info("Demo mode with video recording may cause memory overflow!");
            isOK = true;
        }

        if (isHeadlessModeEnabled() && isDemoModeEnabled()) {
            error("Demo mode with headless make no sense!");
            isOK = false;
        }

        if (isGenerateVideoEnabled() && isExecutionRemoteParallelEnabled()) {
            error("Remote parallel Execution mode with video recording may cause memory overflow! Use Server Video Recording instead!");
            isOK = false;
        }

        if (getNodeJSBinFilePath().isEmpty() && isSkipPlaywrightBrowserDownload() && getWebDriverName().equals(WebDriverName.PLAYWRIGHT)) {
            warn("Node JS bin file path may not well set!");
        }

        if (!isOK) {
            throw new ExceptionBase(ExceptionErrorKeys.PROPERTIES_HAS_CONFLICT);
        }
    }

}
