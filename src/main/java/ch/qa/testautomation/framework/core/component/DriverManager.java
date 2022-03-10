package ch.qa.testautomation.framework.core.component;


import ch.qa.testautomation.framework.common.enumerations.TestType;
import ch.qa.testautomation.framework.common.utils.WebOperationUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.controller.ExternAppController;
import ch.qa.testautomation.framework.core.json.container.JSONDriverConfig;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.mobile.AndroidDriverProvider;
import ch.qa.testautomation.framework.mobile.IOSDriverProvider;
import ch.qa.testautomation.framework.mobile.MobileAppDriverProvider;
import ch.qa.testautomation.framework.rest.RestDriverProvider;
import ch.qa.testautomation.framework.rest.RestfulDriver;
import ch.qa.testautomation.framework.web.ChromeDriverProvider;
import ch.qa.testautomation.framework.web.EdgeDriverProvider;
import ch.qa.testautomation.framework.web.RemoteWebDriverProvider;
import ch.qa.testautomation.framework.web.WebDriverProvider;
import com.fasterxml.jackson.databind.JsonNode;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.*;
import static ch.qa.testautomation.framework.configuration.PropertyResolver.*;

/**
 * Utility class to manage drivers that will be used for further testing
 */
public class DriverManager {

    private static RestDriverProvider restDriverProvider;
    private static RemoteWebDriverProvider remoteWebDriverProvider;
    private static MobileAppDriverProvider mobileAppDriverProvider;
    private static File driverFile;
    private static WebDriverProvider webDriverProvider;
    private static NonDriverProvider nonDriverProvider;

    public static void setupWebDriver() {
        switch (PropertyResolver.getWebDriverName()) {
            case "safari":
                webDriverProvider = installSafariDriver();
                break;
            case "edge":
                webDriverProvider = installEdgeDriver();
                break;
            default:
                webDriverProvider = installChromeDriver(null);
                break;
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


    public static void setupRestDriver() {
        JsonNode restConfig = JSONContainerFactory.getConfig(getRESTConfigFile());
        restDriverProvider = new RestDriverProvider(restConfig.get("user").asText(), restConfig.get("password").asText(), restConfig.get("host").asText());
    }

    public static void setupRemoteWebDriver() {
        try {
            List<JSONDriverConfig> configs = JSONContainerFactory.getDriverConfigs(getDefaultDriverConfigLocation(), getRemoteWebDriverConfig());
            remoteWebDriverProvider = new RemoteWebDriverProvider(configs);
        } catch (IOException e) {
            error(e);
        }
    }

    public static void setupNonDriver() {
        nonDriverProvider = new NonDriverProvider();
    }

    public static void setupMobileAppDriver(TestType type) {
        try {
            List<JSONDriverConfig> configs = JSONContainerFactory.getDriverConfigs(getDefaultDriverConfigLocation(), getMobileAppDriverConfig());
            if (type.equals(TestType.MOBILE_IOS)) {
                mobileAppDriverProvider = new IOSDriverProvider(configs);
            } else if (type.equals(TestType.MOBILE_ANDROID)) {
                mobileAppDriverProvider = new AndroidDriverProvider(configs);
            } else {
                throw new RuntimeException("Type of Mobile App Test is undefined!");
            }
        } catch (IOException e) {
            error(e);
        }
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
            webDriver.get(url);
            displayed = WebOperationUtils.waitForPageLoad(webDriver);
        }
        if (remoteWebDriverProvider != null) {
            RemoteWebDriver remoteWebDriver = getRemoteWebDriver();
            remoteWebDriver.get(url);
            displayed = WebOperationUtils.waitForPageLoad(remoteWebDriver);
        }
        if (!displayed) {
            throw new RuntimeException("Page is not displayed with url: " + url);
        }
    }

    /**
     * close driver
     */
    public static void closeDriver() {
        info("Try to close driver...");
        if (restDriverProvider != null) {
            restDriverProvider.close();
        }
        if (webDriverProvider != null) {
            webDriverProvider.close();
        }
        if (remoteWebDriverProvider != null) {
            remoteWebDriverProvider.close();
        }
        if (mobileAppDriverProvider != null) {
            mobileAppDriverProvider.close();
        }
        if (nonDriverProvider != null) {
            nonDriverProvider.close();
        }
    }

    public static WebDriverProvider getWebDriverProvider() {
        return webDriverProvider;
    }

    public static MobileAppDriverProvider getMobileAppDriverProvider() {
        return mobileAppDriverProvider;
    }

    public static RemoteWebDriverProvider getRemoteWebDriverProvider() {
        return remoteWebDriverProvider;
    }

    public static RestDriverProvider getRestDriverProvider() {
        return restDriverProvider;
    }

    public static WebDriver getWebDriver() {
        if (webDriverProvider != null) {
            return webDriverProvider.getDriver();
        }
        if (remoteWebDriverProvider != null) {
            return remoteWebDriverProvider.getDriver();
        }
        throw new RuntimeException("Nether web driver provider nor remote web driver provider was initialized! Please check test type in test case content!");
    }

    public static RemoteWebDriver getRemoteWebDriver() {
        return remoteWebDriverProvider.getDriver();
    }

    public static RestfulDriver getRestDriver() {
        return restDriverProvider.getDriver();
    }

    public static RestfulDriver getRestDriverwithParam(String host, String user, String password) {
        return restDriverProvider.getDriver(host, user, password);
    }

    public static RestfulDriver getRestDriverWithParam(String host, String encodedKey) {
        return restDriverProvider.getDriver(host, encodedKey);
    }

    public static AppiumDriver getAppDriver() {
        return mobileAppDriverProvider.getDriver();
    }

    private static WebDriverProvider installChromeDriver(ChromeOptions options) {
        try {
            String path = ExternAppController.matchChromeAndDriverVersion().toString();
            driverFile = new File(path);
            driverFile.setExecutable(true);
            setChromeDriverPath(driverFile.getPath());
            PropertyResolver.setChromeDriverFileName(driverFile.getName());
        } catch (IOException ex) {
            warn("Install Chrome driver failed!");
            error(ex);
        }
        if (options == null) {
            return new ChromeDriverProvider();
        } else {
            return new ChromeDriverProvider(options);
        }
    }

    private static WebDriverProvider installEdgeDriver() {
        if (isWindows()) {
            String path = ExternAppController.findEdgeDriver().toString();
            driverFile = new File(path);
            driverFile.setExecutable(true);
            setWebDriverEdgeProperty(driverFile.getPath());
            setEdgeDriverFileName(driverFile.getName());
        } else {
            throw new RuntimeException("Edge Driver can not be run in non windows OS!");
        }
        return new EdgeDriverProvider();
    }

    private static WebDriverProvider installSafariDriver() {
        //todo: safari driver install
        return null;
    }
}