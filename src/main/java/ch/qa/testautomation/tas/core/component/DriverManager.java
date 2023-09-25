package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.utils.OperationSystemUtils;
import ch.qa.testautomation.tas.common.utils.WaitUtils;
import ch.qa.testautomation.tas.core.controller.ExternAppController;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.core.media.ImageHandler;
import ch.qa.testautomation.tas.core.service.RemoteWebDriverConfigService;
import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;
import ch.qa.testautomation.tas.rest.RestDriverProvider;
import ch.qa.testautomation.tas.rest.base.RestDriverBase;
import ch.qa.testautomation.tas.web.ChromeDriverProvider;
import ch.qa.testautomation.tas.web.EdgeDriverProvider;
import ch.qa.testautomation.tas.web.RemoteWebDriverProvider;
import ch.qa.testautomation.tas.web.WebDriverProvider;
import com.codeborne.selenide.WebDriverRunner;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static ch.qa.testautomation.tas.common.enumerations.BrowserName.CHROME;
import static ch.qa.testautomation.tas.common.enumerations.BrowserName.EDGE;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.debug;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.configuration.PropertyResolver.*;
import static com.codeborne.selenide.Selenide.open;

/**
 * Utility class to manage drivers that will be used for testing
 */
public class DriverManager {

    private static RestDriverProvider restDriverProvider;
    private static RemoteWebDriverProvider remoteWebDriverProvider;
    private static File driverFile;
    private static WebDriverProvider webDriverProvider;
    private static NonDriverProvider nonDriverProvider;

    private static final Map<String, String> platforms = new LinkedHashMap<>();

    public static void setupWebDriver() {
        String browserName = getWebDriverName();
        if (EDGE.getName().equals(browserName)) {
            if (!(webDriverProvider instanceof EdgeDriverProvider)) {
                webDriverProvider = installEdgeDriver();
            }
        } else if (CHROME.getName().equals(browserName)) {
            if (!(webDriverProvider instanceof ChromeDriverProvider)) {
                webDriverProvider = installChromeDriver(null);
            }
        } else {
            throw new ApollonBaseException(ApollonErrorKeys.BROWSER_NOT_SUPPORTED, browserName);
        }
    }

    /**
     * reset chrome driver with new options
     *
     * @param options options
     */
    public static void resetChromeDriver(ChromeOptions options) {
        if (Objects.nonNull(webDriverProvider)) {
            webDriverProvider.close();
        }
        webDriverProvider = installChromeDriver(options);
    }

    /**
     * setup rest driver using restConfig.json
     */
    public static void setupRestDriver() {
        JsonNode restConfig = JSONContainerFactory.getConfig(getRestConfigFile());
        restDriverProvider = new RestDriverProvider(restConfig.get("user").asText(), restConfig.get("password").asText(), restConfig.get("host").asText());
    }

    /**
     * setup remote web driver using remote driver config
     */
    public static void setupRemoteWebDriver() {
        RemoteWebDriverConfigService.loadConfigs();
        remoteWebDriverProvider = new RemoteWebDriverProvider(RemoteWebDriverConfigService.lockConfig());
    }

    public static void setupNonDriver() {
        nonDriverProvider = new NonDriverProvider();
    }


    /**
     * open page with url
     *
     * @param url url of page
     */
    public static void openUrl(String url) {
        boolean displayed = false;
        if (webDriverProvider != null) {
            WebDriver webDriver = getWebDriver();
            WebDriverRunner.setWebDriver(webDriver);
            open(url);
            displayed = WaitUtils.waitForPageLoad(30).isDisplayed();
        }
        if (remoteWebDriverProvider != null) {
            RemoteWebDriver remoteWebDriver = getRemoteWebDriver();
            WebDriverRunner.setWebDriver(remoteWebDriver);
            open(url);
            displayed = WaitUtils.waitForPageLoad(30).isDisplayed();
        }
        if (!displayed) {
            info("Page with Url: " + url + " was not displayed after 30 Sec.");
        }
    }

    /**
     * close driver
     */
    public static void closeDriver() {
        if (restDriverProvider != null) {
            restDriverProvider.close();
        }
        if (webDriverProvider != null) {
            webDriverProvider.close();
        }
        if (remoteWebDriverProvider != null) {
            remoteWebDriverProvider.close();
        }
        if (nonDriverProvider != null) {
            nonDriverProvider.close();
        }
    }

    /**
     * clean up driver processes in OS
     */
    public static void cleanUp() {
        info("Clean Up Driver and browser session in Windows System!");
        OperationSystemUtils.cleanUpDriverProcess();
    }

    public static WebDriverProvider getWebDriverProvider() {
        return webDriverProvider;
    }

    public static RemoteWebDriverProvider getRemoteWebDriverProvider() {
        return remoteWebDriverProvider;
    }

    public static RestDriverProvider getRestDriverProvider() {
        return restDriverProvider;
    }

    /**
     * get initialized web driver
     *
     * @return web driver
     */
    public static WebDriver getWebDriver() {
        if (webDriverProvider != null) {
            return webDriverProvider.getDriver();
        }
        if (remoteWebDriverProvider != null) {
            return remoteWebDriverProvider.getDriver();
        }
        throw new ApollonBaseException(ApollonErrorKeys.NO_WEB_DRIVER_INITIALIZED);
    }

    /**
     * get initialized remote web driver
     *
     * @return remote web driver
     */
    public static RemoteWebDriver getRemoteWebDriver() {
        return remoteWebDriverProvider.getDriver();
    }

    /**
     * get initialized rest driver
     *
     * @return rest driver
     */
    public static RestDriverBase getRestDriver() {
        return restDriverProvider.getDriver();
    }

    /**
     * get rest driver with given parameters
     *
     * @param host     host of rest
     * @param user     user of rest
     * @param password password of rest
     * @return rest driver
     */
    public static RestDriverBase getRestDriverWithParam(String host, String user, String password) {
        return restDriverProvider.getDriver(host, user, password);
    }

    /**
     * get rest driver with given parameters
     *
     * @param host               host of rest
     * @param authorizationToken = 'Basic base64.encode(user:password)' or 'Bearer PAT'
     * @return rest driver
     */
    public static RestDriverBase getRestDriverWithParam(String host, String authorizationToken) {
        return restDriverProvider.getDriver(host, authorizationToken);
    }

    private static WebDriverProvider installChromeDriver(ChromeOptions options) {
        String path = ExternAppController.prepareChromeDriver().toString();
        driverFile = new File(path);
        driverFile.setExecutable(true);
        setChromeDriverPath(driverFile.getPath());
        setChromeDriverFileName(driverFile.getName());
        if (options == null) {
            return new ChromeDriverProvider();
        } else {
            return new ChromeDriverProvider(options);
        }
    }

    private static WebDriverProvider installEdgeDriver() {
        String path = ExternAppController.prepareEdgeDriver().toString();
        driverFile = new File(path);
        driverFile.setExecutable(true);
        setEdgeDriverPath(driverFile.getPath());
        setEdgeDriverFileName(driverFile.getName());
        return new EdgeDriverProvider();
    }

    public static void stopRecordingScreen(TestRunResult testRunResult) {
        info("Stop Recording Video.");
        ImageHandler.finishVideoRecording(testRunResult);

    }

    public static void startRecordingScreen() {
        info("Start Recording Video.");
    }

    public static void setCurrentPlatform(String platform) {
        debug("Set Current Platform to: " + platform + " for: " + Thread.currentThread().getName());
        platforms.put(Thread.currentThread().getName(), platform);
    }

    public static String getCurrentPlatform() {
        String platform = platforms.get(Thread.currentThread().getName());
        debug("Get Current Platform " + platform + " for: " + Thread.currentThread().getName());
        return platform;
    }
}