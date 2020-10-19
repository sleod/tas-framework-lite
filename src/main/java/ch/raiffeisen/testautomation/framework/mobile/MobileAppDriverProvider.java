package ch.raiffeisen.testautomation.framework.mobile;

import ch.raiffeisen.testautomation.framework.common.logging.Screenshot;
import ch.raiffeisen.testautomation.framework.core.component.TestStepMonitor;
import ch.raiffeisen.testautomation.framework.core.json.container.JSONDriverConfig;
import ch.raiffeisen.testautomation.framework.intefaces.DriverProvider;
import ch.raiffeisen.testautomation.framework.intefaces.ScreenshotTaker;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static ch.raiffeisen.testautomation.framework.common.logging.ScreenCapture.getScreenshot;
import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.error;
import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.info;

public abstract class MobileAppDriverProvider implements DriverProvider, ScreenshotTaker {

    protected String hubURL;
    protected AppiumDriver<?> driver = null;
    protected DesiredCapabilities capabilities = null;
    protected List<JSONDriverConfig> configs;

    public MobileAppDriverProvider(DesiredCapabilities capabilities, String hubURL) {
        this.capabilities = capabilities;
        this.hubURL = hubURL;
    }

    public MobileAppDriverProvider(List<JSONDriverConfig> jsonDriverConfigs) {
        this.configs = jsonDriverConfigs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AppiumDriver<?> getDriver() {
        if (this.driver == null) {
            initialize();
        }
        return driver;
    }

    public void setDriver(AppiumDriver driver) {
        this.driver = driver;
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Override
    public abstract void initialize();

    public void updateCapability(String key, String value) {
        capabilities.setCapability(key, value);
    }

    @Override
    public Screenshot takeScreenShot(TestStepMonitor testStepMonitor) {
        return getScreenshot(testStepMonitor, driver);
    }

    protected List<JSONDriverConfig> filterConfig(List<JSONDriverConfig> configFiles, Platform platform) {
        List<JSONDriverConfig> result = new LinkedList<>();
        configFiles.forEach(config -> {
            if (config.getPlatformName() != null) {
                if (config.getPlatformName().equalsIgnoreCase(platform.name())) {
                    result.add(config);
                }
            }
        });
        return result;
    }
}
