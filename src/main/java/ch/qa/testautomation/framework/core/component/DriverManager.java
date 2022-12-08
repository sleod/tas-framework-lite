package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.utils.OperationSystemUtils;
import ch.qa.testautomation.framework.common.utils.WaitUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.controller.ExternAppController;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import ch.qa.testautomation.framework.rest.RestDriverProvider;
import ch.qa.testautomation.framework.rest.base.RestDriverBase;
import ch.qa.testautomation.framework.web.ChromeDriverProvider;
import ch.qa.testautomation.framework.web.EdgeDriverProvider;
import ch.qa.testautomation.framework.web.WebDriverProvider;
import com.codeborne.selenide.WebDriverRunner;
import com.fasterxml.jackson.databind.JsonNode;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;

import static ch.qa.testautomation.framework.common.enumerations.BrowserName.CHROME;
import static ch.qa.testautomation.framework.common.enumerations.BrowserName.EDGE;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.warn;
import static ch.qa.testautomation.framework.configuration.PropertyResolver.*;
import static com.codeborne.selenide.Selenide.open;

/**
 * Utility class to manage drivers that will be used for testing
 */
public class DriverManager {

    private static RestDriverProvider restDriverProvider;
    private static File driverFile;
    private static WebDriverProvider webDriverProvider;
    private static NonDriverProvider nonDriverProvider;

    private static String platform = "";

    public static void setupWebDriver() {
        String browserName = getWebDriverName();
        if (EDGE.getName().equals(browserName)) {
            if (!(webDriverProvider instanceof EdgeDriverProvider))
                webDriverProvider = installEdgeDriver();
        } else if (CHROME.getName().equals(browserName)) {
            if (!(webDriverProvider instanceof ChromeDriverProvider))
                webDriverProvider = installChromeDriver(null);
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
        if (webDriverProvider != null) {
            webDriverProvider.close();
        }
        webDriverProvider = installChromeDriver(options);
    }

    /**
     * setup rest driver using restConfig.json
     */
    public static void setupRestDriver() {
        JsonNode restConfig = JSONContainerFactory.getConfig(getRESTConfigFile());
        restDriverProvider = new RestDriverProvider(restConfig.get("user").asText(), restConfig.get("password").asText(), restConfig.get("host").asText());
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
        if (!displayed) {
            warn("Page with Url: " + url + " was not displayed after 30 Sec.");
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
        if (nonDriverProvider != null) {
            nonDriverProvider.close();
        }
    }

    /**
     * clean up driver processes in OS
     */
    public static void cleanUp() {
        if (PropertyResolver.isWindows()) {
            trace("Clean Up Driver and browser session in Windows System!");
            OperationSystemUtils.cleanUpWindowsDriverProcess();
        } else {
            OperationSystemUtils.cleanUpNonWindowsDriverProcess();
        }
    }

    public static WebDriverProvider getWebDriverProvider() {
        return webDriverProvider;
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
        throw new ApollonBaseException(ApollonErrorKeys.NO_WEB_DRIVER_INITIALIZED);
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

    public static void setCurrentPlatform(String platform) {
        DriverManager.platform = platform;
    }

    public static String getCurrentPlatform() {
        return platform;
    }
}