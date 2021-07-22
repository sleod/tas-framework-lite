package ch.raiffeisen.testautomation.framework.web;

import ch.raiffeisen.testautomation.framework.common.logging.Screenshot;
import ch.raiffeisen.testautomation.framework.common.utils.WindowsUtils;
import ch.raiffeisen.testautomation.framework.core.component.TestStepMonitor;
import ch.raiffeisen.testautomation.framework.core.json.container.JSONDriverConfig;
import ch.raiffeisen.testautomation.framework.intefaces.DriverProvider;
import ch.raiffeisen.testautomation.framework.intefaces.ScreenshotTaker;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.LinkedHashMap;
import java.util.Objects;

import static ch.raiffeisen.testautomation.framework.common.logging.ScreenCapture.getScreenshot;
import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.trace;

public abstract class WebDriverProvider implements ScreenshotTaker, DriverProvider {
    private static LinkedHashMap<String, WebDriver> drivers = new LinkedHashMap<>();
    private static JSONDriverConfig config = null;

    public void setDriver(WebDriver driver) {
        drivers.put(Thread.currentThread().getName(), driver);
        trace("Save driver for: " + Thread.currentThread().getName());
    }

    @SuppressWarnings("unchecked")
    public WebDriver getDriver() {
        String tid = Thread.currentThread().getName();
        if (drivers.get(tid) == null) {
            trace("Init driver for: " + tid);
            initialize();
        } else {
            trace("Get driver for: " + tid);
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

    /**
     * Take Screenshot of Step
     *
     * @param testStepMonitor test step monitor
     * @return Screenshot
     */
    @Override
    public synchronized Screenshot takeScreenShot(TestStepMonitor testStepMonitor) {
        RemoteWebDriver driver = (RemoteWebDriver) drivers.get(Thread.currentThread().getName());
        return getScreenshot(testStepMonitor, driver);
    }
}
