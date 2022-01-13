package ch.qa.testautomation.framework.mobile;

import ch.qa.testautomation.framework.core.component.TestRunManager;
import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.core.json.container.JSONDriverConfig;
import io.appium.java_client.ios.IOSDriver;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.error;

public class IOSDriverProvider extends MobileAppDriverProvider {

    public IOSDriverProvider(DesiredCapabilities capabilities, String hubURL) {
        super(capabilities, hubURL);
    }

    public IOSDriverProvider(List<JSONDriverConfig> configs) {
        super(configs);
    }

    @Override
    public IOSDriver getDriver() {
        if (driver == null) {
            initialize();
        }
        //noinspection unchecked
        return (IOSDriver) driver;
    }

    @Override
    public void initialize() {
        if (configs != null && !configs.isEmpty()) {
            for (JSONDriverConfig config : filterConfig(configs, Platform.IOS)) {
                capabilities = new DesiredCapabilities();
                config.getCapabilities().forEach((key, value) -> capabilities.setCapability(key, value));
                hubURL = config.getHubURL();
                if ((config.getAppName() == null || config.getAppName().isEmpty()) && (config.getBundleId() == null || config.getBundleId().isEmpty())) {
                    if (TestRunManager.getCurrentISOAppName() == null) {
                        throw new RuntimeException("App Name / BundleId is not set in capabilities!");
                    } else {
                        capabilities.setCapability("appName", TestRunManager.getCurrentISOAppName());
                    }
                }
                if (capabilities == null) {
                    throw new RuntimeException("Capability is not set!");
                } else {
                    capabilities.asMap().forEach((key, value) -> SystemLogger.trace("Capability: " + key + " -> " + value));
                }
                try {
                    SystemLogger.trace("Try to connect: " + hubURL);
                    driver = new IOSDriver(new URL(hubURL), capabilities);
                    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(180));
                } catch (MalformedURLException | WebDriverException e) {
                    if (driver != null) {
                        driver.quit();
                        driver.close();
                    }
                    error(e);
                }
            }
        }
    }
}