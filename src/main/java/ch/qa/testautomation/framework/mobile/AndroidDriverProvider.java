package ch.qa.testautomation.framework.mobile;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.core.component.TestRunManager;
import ch.qa.testautomation.framework.core.json.container.JSONDriverConfig;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.error;

public class AndroidDriverProvider extends MobileAppDriverProvider {
    public AndroidDriverProvider(DesiredCapabilities capabilities, String hubURL) {
        super(capabilities, hubURL);
    }

    public AndroidDriverProvider(List<JSONDriverConfig> configs) {
        super(configs);
    }

    @Override
    public AndroidDriver getDriver() {
        if (driver == null) {
            initialize();
        }
        return (AndroidDriver) driver;
    }

    @Override
    public void initialize() {
        if (configs != null && !configs.isEmpty()) {
            for (JSONDriverConfig config : filterConfig(configs, Platform.ANDROID)) {
                hubURL = config.getHubURL();
                capabilities = new DesiredCapabilities();
                config.getCapabilities().forEach((key, value) -> capabilities.setCapability(key, value));
                if (config.getIsNoReset().isEmpty()) {
                    capabilities.setCapability("noReset", "true");
                } else {
                    SystemLogger.trace("FullReset activated and NoReset true!");
                }

                if (config.getAppPackage() == null || config.getAppPackage().isEmpty()) {
                    if (TestRunManager.getCurrentAppName() == null) {
                        throw new RuntimeException("App Package Name is not set in capabilities!");
                    } else {
                        capabilities.setCapability("appPackage", TestRunManager.getCurrentAppName());
                        capabilities.setCapability("appActivity", TestRunManager.getCurrentAppActivity());
                    }
                }
                if (capabilities == null) {
                    throw new RuntimeException("Capability is not set!");
                } else {
                    capabilities.asMap().forEach((key, value) -> SystemLogger.trace("Capability: " + key + " -> " + value));
                }
                try {
                    driver = new AndroidDriver(new URL(hubURL), capabilities);
                    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(180));
                    break;
                } catch (MalformedURLException | WebDriverException e) {
                    if (driver != null) {
                        driver.quit();
                    }
                    error(e);
                }
            }
        }
    }
}