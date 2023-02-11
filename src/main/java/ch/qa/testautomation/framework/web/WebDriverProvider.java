package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.common.logging.Screenshot;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.intefaces.DriverProvider;
import ch.qa.testautomation.framework.intefaces.ScreenshotTaker;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ch.qa.testautomation.framework.common.logging.ScreenCapture.getScreenshot;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.info;

/**
 * Web Driver Provider which can be extended for
 */
public abstract class WebDriverProvider implements ScreenshotTaker, DriverProvider {
    private static final Map<String, WebDriver> drivers = new HashMap<>();

    public void setDriver(WebDriver driver) {
        drivers.put(Thread.currentThread().getName(), driver);
        info("Save web driver for: " + Thread.currentThread().getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public WebDriver getDriver() {
        String tid = Thread.currentThread().getName();
        if (drivers.get(tid) == null) {
            info("Init web driver for: " + tid);
            initialize();
        } else {
            info("Get web driver for: " + tid);
        }
        return drivers.get(tid);
    }

    @Override
    public void close() {
        String tid = Thread.currentThread().getName();
        if (Objects.nonNull(drivers.get(tid))) {
            if (PropertyResolver.isKeepBrowser()) {
                info("Keep browser session for: " + tid);
            } else {
                info("Close web driver for: " + tid);
                drivers.get(tid).quit();
            }
            drivers.remove(tid);
        }
    }

    @Override
    public abstract void initialize();

    /**
     * Take Screenshot of Step
     *
     * @return Screenshot
     */
    @Override
    public synchronized Screenshot takeScreenShot() {
        return getScreenshot((TakesScreenshot) getDriver());
    }

    public static void configureWindowSize(WebDriver driver, boolean useMaxSize) {
        int width;
        int height;
        if (useMaxSize) {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            width = size.width;
            height = size.height;
        } else {
            String[] dimensions = PropertyResolver.getScreenSize().split(",");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
        }
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(width, height));
        info("Used screen size width: " + width + " height: " + height);
    }
}
