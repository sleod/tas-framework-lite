package ch.sleod.testautomation.framework.web;

import ch.sleod.testautomation.framework.common.enumerations.BrowserName;
import ch.sleod.testautomation.framework.common.logging.Screenshot;
import ch.sleod.testautomation.framework.core.component.TestStepMonitor;
import ch.sleod.testautomation.framework.core.json.container.JSONDriverConfig;
import ch.sleod.testautomation.framework.intefaces.DriverProvider;
import ch.sleod.testautomation.framework.intefaces.ScreenshotTaker;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.*;

public class RemoteWebDriverProvider implements DriverProvider, ScreenshotTaker {

    private RemoteWebDriver driver = null;
    private DesiredCapabilities capabilities = null;
    private BrowserName browserName;
    private Platform platform;
    private String hubURL;
    private String grdnUUID;
    private String realDeviceUuid;
    private List<JSONDriverConfig> configs;


    /**
     * @param browserName    browser name
     * @param platform       platform
     * @param hubURL         gird hub url
     * @param realDeviceUuid uuid for test bird
     */
    @Deprecated
    public RemoteWebDriverProvider(String browserName, String platform, String hubURL, String realDeviceUuid) {
        this.browserName = BrowserName.valueOf(browserName.toUpperCase());
        this.platform = Platform.valueOf(platform.toUpperCase());
        this.hubURL = hubURL;
        this.realDeviceUuid = realDeviceUuid;
    }

    @Deprecated
    public RemoteWebDriverProvider(DesiredCapabilities capabilities, String hubURL) {
        this.capabilities = capabilities;
        this.hubURL = hubURL;
    }

    public RemoteWebDriverProvider(List<JSONDriverConfig> configs) {
        this.configs = configs;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RemoteWebDriver getDriver() {
        if (this.driver == null) {
            initialize();
        }
        return this.driver;
    }

    public void setDriver(RemoteWebDriver driver) {
        this.driver = driver;
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Override
    public void initialize() {
        if (configs != null && !configs.isEmpty()) {
            for (JSONDriverConfig config : configs) {
                if (config.getHubURL() != null && !config.getHubURL().isEmpty()) {
                    browserName = BrowserName.valueOf(config.getBrowserName().toUpperCase());
                    grdnUUID = config.getGrdn_uuid();
                    realDeviceUuid = config.getRealDeviceUuid();
                    platform = Platform.valueOf(config.getPlatformName().toUpperCase());
                    hubURL = config.getHubURL();
                    if (capabilities == null) {
                        switch (browserName) {
                            case FIREFOX:
                                capabilities = DesiredCapabilities.firefox();
                                break;
                            case CHROME:
                                capabilities = new DesiredCapabilities();
                                break;
                            case ANDROID:
                                capabilities = DesiredCapabilities.android();
                                break;
                            case IPAD:
                                capabilities = DesiredCapabilities.ipad();
                                break;
                            case IPHONE:
                                capabilities = DesiredCapabilities.iphone();
                                break;
                            case EDGE:
                                capabilities = DesiredCapabilities.edge();
                                break;
                            case IE:
                                capabilities = DesiredCapabilities.internetExplorer();
                                break;
                            case SAFARI:
                                capabilities = DesiredCapabilities.safari();
                                break;
                        }
                        capabilities.setBrowserName(config.getBrowserName());
                        capabilities.setCapability("platformName", config.getPlatformName());
                        if (grdnUUID != null && !grdnUUID.isEmpty()) {
                            capabilities.setCapability("grdn_uuid", grdnUUID);
                        } else if (realDeviceUuid != null && !realDeviceUuid.isEmpty()) {
                            capabilities.setCapability("testbirds:options", Collections.singletonMap("realDeviceUuid", realDeviceUuid));
                        } else {
                            throw new RuntimeException("grdnUUID or realDeviceUuid not set!!");
                        }
                        capabilities.toJson().forEach((key, value) -> trace("Capability: " + key + " -> " + value));
                    }
                    try {
                        driver = new RemoteWebDriver(new URL(hubURL), capabilities);
                        driver.manage().timeouts().implicitlyWait(180, TimeUnit.SECONDS);
                        break;
                    } catch (MalformedURLException | WebDriverException e) {
                        if (driver != null) {
                            driver.close();
                        }
                        error(e);
                    }
                }
            }
        }
    }

    @Override
    public Screenshot takeScreenShot(TestStepMonitor testStepMonitor) {
        String testCaseName = testStepMonitor.getCurrentTestCase().getDisplayName();
        String stepName = testStepMonitor.getLastStep().getMethodName();
        Screenshot screenshot = null;
        if (driver != null) {
            info("*** save screenshot to Report for Test Case: " + testCaseName + " -> " + stepName);
            byte[] imageData = driver.getScreenshotAs(OutputType.BYTES);
            screenshot = new Screenshot(imageData, testCaseName, stepName);
        }
        return screenshot;
    }
}
