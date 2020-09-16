package ch.sleod.testautomation.framework.core.component;


import ch.sleod.testautomation.framework.common.enumerations.TestType;
import ch.sleod.testautomation.framework.common.utils.WebOperationUtils;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.controller.ExternAppController;
import ch.sleod.testautomation.framework.core.json.container.JSONDriverConfig;
import ch.sleod.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.sleod.testautomation.framework.mobile.AndroidDriverProvider;
import ch.sleod.testautomation.framework.mobile.IOSDriverProvider;
import ch.sleod.testautomation.framework.mobile.MobileAppDriverProvider;
import ch.sleod.testautomation.framework.rest.RestDriverProvider;
import ch.sleod.testautomation.framework.rest.RestfulDriver;
import ch.sleod.testautomation.framework.web.ChromeDriverProvider;
import ch.sleod.testautomation.framework.web.IEDriverProvider;
import ch.sleod.testautomation.framework.web.RemoteWebDriverProvider;
import ch.sleod.testautomation.framework.web.WebDriverProvider;
import io.appium.java_client.AppiumDriver;
import net.sf.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;
import static ch.sleod.testautomation.framework.common.logging.SystemLogger.info;
import static ch.sleod.testautomation.framework.configuration.PropertyResolver.*;

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

    public static WebDriverProvider setupWebDriver() {
//        WebDriverProvider.loadConfig();
//        String driverName = getDefaultWebDriverName();
        switch (PropertyResolver.getWebDriverName()) {
            case "chrome":
                webDriverProvider = installChromeDriver();
                break;
            case "ie":
                webDriverProvider = installIEDriver();
                break;
        }
        return webDriverProvider;
    }

    public static RestDriverProvider setupRestDriver() {
        JSONObject restConfig = JSONContainerFactory.getConfig(getRESTConfigFile());
        restDriverProvider = new RestDriverProvider(restConfig.getString("user"), restConfig.getString("password"), restConfig.getString("host"));
        return restDriverProvider;
    }

    public static RemoteWebDriverProvider setupRemoteWebDriver() {
        try {
            List<JSONDriverConfig> configs = JSONContainerFactory.getDriverConfigs(getDefaultDriverConfigLocation(), getRemoteWebDriverConfig());
            remoteWebDriverProvider = new RemoteWebDriverProvider(configs);
        } catch (IOException e) {
            error(e);
        }
        return remoteWebDriverProvider;
    }

    public static void setupNonDriver() {
        nonDriverProvider = new NonDriverProvider();
    }

    public static MobileAppDriverProvider setupMobileAppDriver(TestType type) {
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
        return mobileAppDriverProvider;
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

    public static RestfulDriver getRestDriverwithParam(String host, String encodedKey) {
        return restDriverProvider.getDriver(host, encodedKey);
    }

    public static AppiumDriver<?> getAppDriver() {
        return mobileAppDriverProvider.getDriver();
    }

    private static WebDriverProvider installChromeDriver() {
        String chromeDriverPath = System.getenv("CHROME_DRIVER_EXECUTABLE_PATH");
        if (chromeDriverPath != null) {
            setChromeDriverPath(chromeDriverPath);
        } else {
            chromeDriverPath = PropertyResolver.getChromeDriverPath();
//            String resource = driverBinDir + getChromeDriverFileName();
//            if (isWindows()) {
//                resource += ".exe";
//            }
//            String path = FileLocator.findResource(resource).toString();
            if (chromeDriverPath == null) {
                try {
                    String path = ExternAppController.matchChromeAndDriverVersion().toString();
                    driverFile = new File(path);
                    driverFile.setExecutable(true);
                    setChromeDriverPath(driverFile.getPath());
                } catch (IOException ex) {
                    error(ex);
                }
            }
        }
        return new ChromeDriverProvider();
    }

    private static WebDriverProvider installIEDriver() {
        String ieDriverPath = System.getenv("IE_DRIVER_EXECUTABLE_PATH");
        if (ieDriverPath != null) {
            setWebDriverIEProperty(ieDriverPath);
        } else {
            if (isWindows()) {
                String path = Objects.requireNonNull(DriverManager.class.getClassLoader().getResource(getDefaultWebDriverBinLocation() + getIEDriverFileName())).getPath();
                driverFile = new File(path);
            }
            driverFile.setExecutable(true);
            setWebDriverIEProperty(driverFile.getPath());
        }
        return new IEDriverProvider();
    }
}