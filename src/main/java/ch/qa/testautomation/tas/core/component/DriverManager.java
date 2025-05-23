package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.enumerations.WebDriverName;
import ch.qa.testautomation.tas.common.processhandling.CrossPlatformProcessScanner;
import ch.qa.testautomation.tas.common.utils.WaitUtils;
import ch.qa.testautomation.tas.core.controller.ExternAppController;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.core.service.RemoteWebDriverConfigService;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import ch.qa.testautomation.tas.intefaces.DriverProvider;
import ch.qa.testautomation.tas.rest.base.RestDriverProvider;
import ch.qa.testautomation.tas.rest.base.TASRestDriver;
import ch.qa.testautomation.tas.web.*;
import com.codeborne.selenide.WebDriverRunner;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.util.Objects;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.debug;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.configuration.PropertyResolver.*;
import static com.codeborne.selenide.Selenide.open;

/**
 * Utility class to manage drivers that will be used for testing
 */
public class DriverManager {

    private static final ThreadLocal<String> platforms = new ThreadLocal<>();
    private static final ThreadLocal<DriverProvider> driverProviders = new ThreadLocal<>();

    public static void setupWebDriver() {
        String driverName = getWebDriverName();
        if (WebDriverName.CHROME.getName().equals(driverName)) {
            if (!(driverProviders.get() instanceof ChromeDriverProvider)) {
                driverProviders.set(installChromeDriver(null));
            }
        } else if (WebDriverName.PLAYWRIGHT.getName().equals(driverName)) {
            if (!(driverProviders.get() instanceof PlaywrightDriverProvider)) {
                driverProviders.set(new PlaywrightDriverProvider());
            }
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.BROWSER_NOT_SUPPORTED, driverName);
        }
    }

    /**
     * reset chrome driver with new options
     *
     * @param options options
     */
    public static void resetChromeDriver(ChromeOptions options) {
        if (Objects.nonNull(driverProviders.get())) {
            driverProviders.get().close();
        }
        driverProviders.set(installChromeDriver(options));
    }

    /**
     * setup rest driver using restConfig.json
     */
    public static void setupRestDriver() {
        JsonNode restConfig = JSONContainerFactory.getConfig(getRestConfigFile());
        driverProviders.set(new RestDriverProvider(restConfig.get("user").asText(), restConfig.get("password").asText(), restConfig.get("host").asText()));
    }

    /**
     * setup remote web driver using remote driver config
     */
    public static void setupRemoteWebDriver() {
        RemoteWebDriverConfigService.loadConfigs();
        driverProviders.set(new RemoteWebDriverProvider(RemoteWebDriverConfigService.lockConfig()));
    }

    public static void setupNonDriver() {
        driverProviders.set(new NonDriverProvider());
    }

    /**
     * open page with url
     *
     * @param url url of page
     */
    public static void openUrl(String url) {
        boolean displayed = false;
        DriverProvider driverProvider = driverProviders.get();
        if (driverProvider instanceof RemoteWebDriverProvider) {
            RemoteWebDriver remoteWebDriver = getRemoteWebDriver();
            WebDriverRunner.setWebDriver(remoteWebDriver);
            open(url);
            displayed = WaitUtils.waitForPageLoad(30).isDisplayed();
        } else if (driverProvider instanceof WebDriverProvider) {
            WebDriver webDriver = getWebDriver();
            WebDriverRunner.setWebDriver(webDriver);
            open(url);
            displayed = WaitUtils.waitForPageLoad(30).isDisplayed();
        } else if (driverProvider instanceof PlaywrightDriverProvider) {
            getPlaywrightDriver().open(url);
            displayed = getPlaywrightDriver().find("body").isDisplayed();
        }

        if (!displayed) {
            info("Page with Url: " + url + " was not displayed after 30 Sec.");
        }
    }

    public static DriverProvider getDriverProvider() {
        if (Objects.nonNull(driverProviders.get())) {
            return driverProviders.get();
        }
        throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Driver provider was not initialized!");
    }

    /**
     * close driver
     */
    public static void closeDriver() {
        if (Objects.nonNull(driverProviders.get())) {
            driverProviders.get().close();
        }
    }

    /**
     * clean up driver processes in OS
     */
    public static void cleanUp() {
        info("Clean Up remain driver session in Operation System.");
        CrossPlatformProcessScanner cpps = new CrossPlatformProcessScanner();
        cpps.run();
    }

    /**
     * get initialized web driver
     *
     * @return web driver
     */
    public static WebDriver getWebDriver() {
        if (driverProviders.get() instanceof RemoteWebDriverProvider remoteWebDriverProvider) {
            return remoteWebDriverProvider.getDriver();
        } else if (driverProviders.get() instanceof WebDriverProvider webDriverProvider) {
            return webDriverProvider.getDriver();
        }
        throw new ExceptionBase(ExceptionErrorKeys.NO_WEB_DRIVER_INITIALIZED);
    }

    /**
     * get initialized web driver
     *
     * @return web driver
     */
    public static PlaywrightDriver getPlaywrightDriver() {
        if (driverProviders.get() instanceof PlaywrightDriverProvider playwrightDriverProvider) {
            return playwrightDriverProvider.getDriver();
        }
        throw new ExceptionBase(ExceptionErrorKeys.NO_WEB_DRIVER_INITIALIZED);
    }

    /**
     * get initialized remote web driver
     *
     * @return remote web driver
     */
    public static RemoteWebDriver getRemoteWebDriver() {
        if (driverProviders.get() instanceof RemoteWebDriverProvider remoteWebDriverProvider) {
            return remoteWebDriverProvider.getDriver();
        }
        throw new ExceptionBase(ExceptionErrorKeys.NO_WEB_DRIVER_INITIALIZED);
    }

    /**
     * get initialized rest driver
     *
     * @return rest driver
     */
    public static TASRestDriver getRestDriver() {
        if (driverProviders.get() instanceof RestDriverProvider restDriverProvider) {
            return restDriverProvider.getDriver();
        }
        throw new ExceptionBase(ExceptionErrorKeys.NO_REST_DRIVER_INITIALIZED);
    }

    /**
     * get rest driver with given parameters
     *
     * @param host     host of rest
     * @param user     user of rest
     * @param password password of rest
     * @return rest driver
     */
    public static TASRestDriver getRestDriverWithParam(String host, String user, String password) {
        if (driverProviders.get() instanceof RestDriverProvider restDriverProvider) {
            return restDriverProvider.getDriver(host, user, password);
        }
        throw new ExceptionBase(ExceptionErrorKeys.NO_REST_DRIVER_INITIALIZED);
    }

    /**
     * get rest driver with given parameters
     *
     * @param host               host of rest
     * @param authorizationToken = 'Basic base64.encode(user:password)' or 'Bearer PAT'
     * @return rest driver
     */
    public static TASRestDriver getRestDriverWithParam(String host, String authorizationToken) {
        if (driverProviders.get() instanceof RestDriverProvider restDriverProvider) {
            return restDriverProvider.getDriver(host, authorizationToken);
        }
        throw new ExceptionBase(ExceptionErrorKeys.NO_REST_DRIVER_INITIALIZED);
    }

    private static WebDriverProvider installChromeDriver(ChromeOptions options) {
        String path = ExternAppController.prepareChromeDriver().toString();
        File driverFile = new File(path);
        if (driverFile.setExecutable(true)) {
            debug("File Setting successful!");
        } else {
            debug("File Setting unsuccessful!");
        }
        setChromeDriverPath(driverFile.getPath());
        setChromeDriverFileName(driverFile.getName());
        if (options == null) {
            return new ChromeDriverProvider();
        } else {
            return new ChromeDriverProvider(options);
        }
    }

    /**
     * only for mobile App Test with Appium and web app with Playwright
     */
    public static void stopRecordingScreen(TestRunResult testRunResult) {
        info("Stop Recording Video.");
        if (driverProviders.get() instanceof PlaywrightDriverProvider) {
            testRunResult.setVideoFilePath(getPlaywrightDriver().getVideoFile().getAbsolutePath());
        }
    }

    /**
     * only for mobile App Test with Appium and web app with playwright
     */
    public static void startRecordingScreen() {
        info("Start Recording Video.");
    }

    public static void setCurrentPlatform(String platform) {
        debug("Set Current Platform to: " + platform + " for: " + Thread.currentThread().getName());
        platforms.set(platform);
    }

    public static String getCurrentPlatform() {
        String platform = platforms.get();
        debug("Get Current Platform " + platform + " for: " + Thread.currentThread().getName());
        return platform;
    }
}