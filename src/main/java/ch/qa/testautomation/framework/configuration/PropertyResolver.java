package ch.qa.testautomation.framework.configuration;

import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.PropertyKey;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static ch.qa.testautomation.framework.common.enumerations.PropertyKey.*;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.*;
import static ch.qa.testautomation.framework.common.utils.StringTextUtils.isValid;
import static java.util.Arrays.asList;

public class PropertyResolver {
    private final static Map<String, Properties> propertyThreadsMap = new HashMap<>();

    public static Properties getProperties() {
        String tid = Thread.currentThread().getName();
        if (!propertyThreadsMap.containsKey(tid)) {
            propertyThreadsMap.put(tid, new Properties());
        }
        return propertyThreadsMap.get(tid);
    }

    public static void loadTestRunProperties(String filePath) {
        if (isValid(filePath)) {
            try {
                InputStream fileStream = FileOperation.retrieveFileFromResourcesAsStream(filePath);
                if (fileStream != null) {
                    getProperties().load(fileStream);
                } else {
                    debug(filePath + " can be not loaded into properties!");
                }
                if (getProperties().isEmpty()) {
                    info("No custom defined properties loaded. Using default configuration...");
                }
            } catch (IOException ex) {
                throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_GENERAL, ex, "load run properties");
            }
        } else {
            debug("Path for properties file is not valid: " + filePath + ". No custom properties file will be loaded.");
        }
    }

    public static String decodeBase64(String encoded) {
        if (encoded != null && org.apache.commons.codec.binary.Base64.isBase64(encoded.getBytes())) {
            byte[] context = Base64.getDecoder().decode(encoded);
            return new String(context);
        } else {
            debug("Given String is null or not suitable for base64 decode! Original String will be used!");
            return encoded;
        }
    }

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
        return metaString.isEmpty() ? Collections.emptyList() : asList(metaString.split(","));
    }

    public static String getScreenshotFormat() {
        return getProperty(SCREENSHOT_FORMAT.key(), "PNG");
    }

    public static String getTestautomationPackage() {
        return getProperty(TEST_AUTOMATION_PACKAGE.key(), "ch.qa.testautomation");
    }

    public static String getDataFormat() {
        return getProperty(DATE_FORMAT.key(), "yyyy-MM-dd");
    }

    public static String getLoggerName() {
        return getProperty(LOGGER_NAME.key(), "SystemLogger");
    }

    public static boolean isAllureReportServiceEnabled() {
        return getProperty(ALLURE_REPORT_SERVICE.key(), "false").equalsIgnoreCase("true");
    }

    public static String getAllureResultsDir() {
        return getProperty(ALLURE_RESULTS_LOCATION.key(), "target/allure-results/");
    }

    public static String getAllureReportDir() {
        return getProperty(ALLURE_REPORT_LOCATION.key(), "target/allure-reports/");
    }

    public static void setChromeDriverPath(String path) {
        System.setProperty(WEBDRIVER_CHROME_DRIVER.key(), path);
    }

    public static String getChromeDriverPath() {
        return getProperty(WEBDRIVER_CHROME_DRIVER.key());
    }

    public static void setEdgeDriverPath(String path) {
        System.setProperty(WEBDRIVER_EDGE_DRIVER.key(), path);
    }

    public static String getEdgeDriverPath() {
        return getProperty(WEBDRIVER_EDGE_DRIVER.key());
    }

    public static String getWebDriverName() {
        return getProperty(DRIVER_BROWSER_NAME.key(), "chrome");
    }

    public static void setWebDriverName(String driverName) {
        setProperty(DRIVER_BROWSER_NAME.key(), driverName);
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

    public static String getChromeDriverFileName() {
        return getProperty(WEBDRIVER_CHROME_FILENAME.key(), "chromedriver");
    }

    public static void setChromeDriverFileName(String fileName) {
        setProperty(WEBDRIVER_CHROME_FILENAME.key(), fileName);
    }

    public static boolean isRestartDriverAfterExecution() {
        return getProperty(RUN_DRIVER_RESTART.key(), "true").equalsIgnoreCase("true");
    }

    public static boolean isKeepBrowser() {
        return getProperty(DEBUG_KEEP_BROWSER.key(), "false").equalsIgnoreCase("true");
    }

    public static void setKeepBrowser(boolean isKeep) {
        setProperty(DEBUG_KEEP_BROWSER.key(), String.valueOf(isKeep));
    }

    public static String getSystemUser() {
        return getProperty("user.name");
    }

    public static String getSystemFileSeparator() {
        return getProperty("file.separator");
    }

    public static String getTextEditor() {
        return getProperty(TEXT_EDITOR.key(), "notepad");
    }

    public static boolean useHeadlessMode() {
        //exist @NonHeadless method then return false
        if (getProperty(METHOD_NONHEADLESS_EXISTS.key(), "false").equalsIgnoreCase("true")) {
            return false;
        } else {
            return getProperty(DRIVER_BROWSER_HEADLESS.key(), "true").equalsIgnoreCase("true");
        }
    }

    public static boolean demoModeEnabled() {
        return getProperty(DEMO_MODE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean useMaximised() {
        return getProperty(DRIVER_BROWSER_FULLSCREEN.key(), "false").equalsIgnoreCase("true");
    }

    public static String getScreenSize() {
        return getProperty(BROWSER_SCREEN_SIZE.key(), "1920,1080");
    }

    public static String getDriverConfigLocation() {
        return getProperty(DRIVER_CONFIG_LOCATION.key(), "driverConfig/");
    }

    public static String getPageConfigLocation() {
        return getProperty(PAGE_CONFIG_LOCATION.key(), "pageDefinitions/");
    }

    public static String getJiraConfigFile() {
        String filename = getProperty(JIRA_CONFIG.key(), "jiraConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getJiraExecConfigFile() {
        String filename = getProperty(JIRA_EXEC_CONFIG.key(), "jiraExecutionConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static boolean isStopRunOnError() {
        return getProperty(RUN_STOP_ON_ERROR.key(), "true").equalsIgnoreCase("true");
    }

    public static String getLogLevelApollon() {
        return getProperty(LOG_LEVEL_APOLLON.key(), Level.INFO.name());
    }

    public static void setIsDebugMode(boolean isDebugMode) {
        setProperty(DEBUG_TRACE_OUTPUT.key(), String.valueOf(isDebugMode));
    }

    public static String getTFSRunnerConfigFile() {
        String filename = getProperty(TFS_RUNNER_CONFIG.key(), "tfsRunnerConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getReportServiceRunnerConfigFile() {
        String filename = getProperty(REPORT_SERVICE_RUNNER_CONFIG.key(), "reportServiceRunnerConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static boolean isTFSConnectEnabled() {
        return getProperty(RUN_TFS_CONNECT.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isTFSSyncEnabled() {
        return getProperty(RUN_TFS_SYNC.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isJIRAConnectEnabled() {
        return getProperty(RUN_JIRA_CONNECT.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isJIRASyncEnabled() {
        return getProperty(RUN_JIRA_SYNC.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isQCSyncEnabled() {
        return getProperty(RUN_QC_SYNC.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRebaseAllureReport() {
        return getProperty(ALLURE_REPORT_REBASE.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean showAllEnvironmentVariables() {
        return getProperty(ALLURE_REPORT_ALL_ENVIRONMENT_VARIABLES.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isRetryEnabled() {
        return getProperty(RETRY_MODE_ENABLED.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isGenerateVideoEnabled() {
        return getProperty(GENERATE_VIDEO.key(), "false").equalsIgnoreCase("true");
    }

    public static List<String> getAllPropertiesWith(String subKey) {
        List<String> settings = new LinkedList<>();
        for (String name : EnumUtils.getEnumMap(PropertyKey.class).keySet()) {
            if (name.toLowerCase().contains(subKey.toLowerCase())) {
                String key = PropertyKey.valueOf(name).key();
                String setting = getProperty(key);
                if (setting == null) {
                    warn("Property for key: " + key + " was not set!!");
                }
                settings.add(setting);
            }
        }
        return settings;
    }

    public static void setProperty(String key, String value) {
        getProperties().setProperty(key, value.trim());
        if (!key.toLowerCase().contains("pat") && !key.toLowerCase().contains("password")) {
            info("Set " + key + " -> " + value);
        } else {
            info("Set " + key + " -> XXXXXXXXXXXXXXXX");
        }
    }

    public static String getProperty(String key) {
        return System.getProperties().containsKey(key) ? System.getProperty(key) : getProperties().getProperty(key);
    }

    private static String getProperty(String key, String defValue) {
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

    public static String getRESTConfigFile() {
        String filename = getProperty(REST_CONFIG.key(), "restConfig.json");
        return getDriverConfigLocation() + filename;
    }

    public static String getVideoFormat() {
        return getProperty(VIDEO_FORMAT.key(), "mp4");
    }

    public static void setProperties(Map<String, String> properties) {
        properties.forEach(PropertyResolver::setProperty);
    }

    public static boolean isLinux() {
        return getProperty("os.name").toLowerCase().contains("linux");
    }

    public static boolean isMac() {
        return getProperty("os.name").toLowerCase().contains("mac");
    }

    public static String getDriverResourceLocation() {
        return getProperty(RESOURCE_DRIVER_LOCATION.key(), "Git - RCH Framework Solution Items/Java/DriverVersions/");
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
        String dir = System.getProperty("user.home") + "/Downloads/";
        if (isWindows()) {
            dir = dir.replace("/", "\\");
        }
        return getProperty(DOWNLOAD_LOCATION.key(), dir);
    }

    public static int getDriverWaitTimeout() {
        String timeout = getProperty(DRIVER_WAIT_TIMEOUT.key(), "5");
        return Integer.parseInt(timeout) * 1000;
    }

    public static String getJiraUser() {
        return getProperty(JIRA_USER.key());
    }

    public static String getJiraPassword() {
        return getProperty(JIRA_PASSWORD.key());
    }

    public static String getJiraPAT() {
        return getProperty(JIRA_PAT.key());
    }

    public static String getJiraHost() {
        return getProperty(JIRA_HOST.key());
    }

    public static String getRestUser() {
        return getProperty(REST_USER.key());
    }

    public static String getRestPassword() {
        return getProperty(REST_PASSWORD.key());
    }

    public static String getRestPAT() {
        return getProperty(REST_PAT.key());
    }

    public static String getRestHost() {
        return getProperty(REST_HOST.key());
    }

    public static String getDBHost() {
        return getProperty(DB_HOST.key());
    }

    public static String getDBUser() {
        return getProperty(DB_USER.key());
    }

    public static String getDBPassword() {
        return getProperty(DB_PASSWORD.key());
    }

    public static String getDBType() {
        return getProperty(DB_TYPE.key());
    }

    public static String getDBName() {
        return getProperty(DB_NAME.key());
    }

    public static String getDBPort() {
        return getProperty(DB_PORT.key());
    }

    public static void setNonHeadlessMethodExists(String booleanString) {
        setProperty(METHOD_NONHEADLESS_EXISTS.key(), booleanString);
    }

    public static boolean hasNonHeadlessMethod() {
        return getProperty(METHOD_NONHEADLESS_EXISTS.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isOpenPDFInSystemReader() {
        return getProperty(OPEN_PDF_IN_SYSTEM_READER.key(), "false").equalsIgnoreCase("true");
    }

    public static boolean isSimpleStringParameterAllowed() {
        return getProperty(SIMPLE_STRING_PARAMETER_ALLOWED.key(), "false").equalsIgnoreCase("true");
    }

    public static void healthCheck() {
        info("Check setting conflict ... ");
        boolean isOK = true;
        if (useHeadlessMode() && isKeepBrowser()) {
            warn(DRIVER_BROWSER_HEADLESS.key() + " and " + DEBUG_KEEP_BROWSER.key() + " can not be 'true' at same time!");
            isOK = false;
        }

        if (isKeepBrowser() && !isStopRunOnError()) {
            warn(DEBUG_KEEP_BROWSER + "=true make sense only wenn " + RUN_STOP_ON_ERROR + "=true !");
            isOK = false;
        }

        if (!isRestartDriverAfterExecution() && isKeepBrowser()) {
            warn(DEBUG_KEEP_BROWSER + "=true and " + RUN_DRIVER_RESTART + "=false will force a Driver Restart after Execution!");
        }

        if (isRetryEnabled() && !isStopRunOnError()) {
            warn(RETRY_MODE_ENABLED + "=true required the " + RUN_STOP_ON_ERROR + " also be true!");
            isOK = false;
        }

        if (isGenerateVideoEnabled() && demoModeEnabled()) {
            warn("Demo mode with video recording may cause memory overflow!");
            isOK = true;
        }

        if (useHeadlessMode() && demoModeEnabled()) {
            warn("Demo mode with headless make no sense!");
            isOK = false;
        }

        if (isJIRASyncEnabled() && !isJIRAConnectEnabled() || isTFSSyncEnabled() && !isTFSConnectEnabled()) {
            warn("To enable JIRA or TFS Sync need to enable connection first!");
            isOK = false;
        }

        if (!isOK) {
            throw new ApollonBaseException(ApollonErrorKeys.PROPERTIES_HAS_CONFLICT);
        }
    }

}
