package ch.sleod.testautomation.framework.web;

import ch.sleod.testautomation.framework.common.logging.Screenshot;
import ch.sleod.testautomation.framework.common.utils.WindowsUtils;
import ch.sleod.testautomation.framework.core.component.TestStepMonitor;
import ch.sleod.testautomation.framework.core.json.container.JSONDriverConfig;
import ch.sleod.testautomation.framework.intefaces.DriverProvider;
import ch.sleod.testautomation.framework.intefaces.ScreenshotTaker;
import ch.sleod.testautomation.framework.common.logging.ScreenCapture;
import ch.sleod.testautomation.framework.common.logging.SystemLogger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.LinkedHashMap;
import java.util.Objects;

public abstract class WebDriverProvider implements ScreenshotTaker, DriverProvider {
    private static LinkedHashMap<String, WebDriver> drivers = new LinkedHashMap<>();
    private static JSONDriverConfig config = null;

    public void setDriver(WebDriver driver) {
        drivers.put(Thread.currentThread().getName(), driver);
        SystemLogger.trace("Save driver for: " + Thread.currentThread().getName());
    }

    @SuppressWarnings("unchecked")
    public WebDriver getDriver() {
        String tid = Thread.currentThread().getName();
        if (drivers.get(tid) == null) {
            SystemLogger.trace("Init driver for: " + tid);
            initialize();
        } else {
            SystemLogger.trace("Get driver for: " + tid);
        }
        return drivers.get(tid);
    }

    @Override
    public void close() {
        if (!drivers.isEmpty()) {
            drivers.values().stream().filter(Objects::nonNull).forEach(WebDriver::quit);
            drivers.clear();
        }
        WindowsUtils.cleanUpWindowsDriverProcess();
    }

    @Override
    public abstract void initialize();

//    /**
//     * load config from file path
//     */
//    public synchronized static void loadConfig() {
//        try {
//            if (config == null) {
//                config = JSONContainerFactory.getDriverConfig(getDefaultDriverConfigLocation() + getWebDriverConfig());
//                PropertyResolver.setDefaultWebDriverName(config.getBrowserName().toLowerCase());
//                if (config.getChromeDriverBinFile() != null) {
//                    PropertyResolver.setChromeDriverFileName(config.getChromeDriverBinFile());
//                }
//                if (config.getIEBinFile() != null) {
//                    PropertyResolver.setIEDriverFileName(config.getIEBinFile());
//                }
//            }
//        } catch (IOException ex) {
//            error(ex);
//        }
//    }

    /**
     * Take Screenshot of Step
     *
     * @param testStepMonitor test step monitor
     * @return Screenshot
     */
    @Override
    public synchronized Screenshot takeScreenShot(TestStepMonitor testStepMonitor) {
        RemoteWebDriver driver = (RemoteWebDriver) drivers.get(Thread.currentThread().getName());
        return ScreenCapture.getScreenshot(testStepMonitor, driver);
    }
}
