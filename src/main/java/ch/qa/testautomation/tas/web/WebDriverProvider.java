package ch.qa.testautomation.tas.web;

import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.DriverManager;
import ch.qa.testautomation.tas.intefaces.DriverProvider;
import org.openqa.selenium.WebDriver;

import java.awt.*;
import java.util.Objects;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;

/**
 * Web Driver Provider which can be extended for
 */
public abstract class WebDriverProvider implements DriverProvider {

    protected static final ThreadLocal<WebDriver> drivers = new ThreadLocal<>();

    public void setDriver(WebDriver driver) {
        drivers.set(driver);
        info("Save web driver for: " + Thread.currentThread().getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public WebDriver getDriver() {
        String tid = Thread.currentThread().getName();
        if (drivers.get() == null) {
            info("Init web driver for: " + tid);
            initialize();
        } else {
            info("Get web driver for: " + tid);
        }
        return drivers.get();
    }

    @Override
    public void close() {
        String tid = Thread.currentThread().getName();
        if (Objects.nonNull(drivers.get())) {
            if (PropertyResolver.isKeepBrowserOnErrorEnabled()) {
                info("Keep browser session for: " + tid);
            } else {
                info("Close web driver for: " + tid);
                drivers.get().quit();
                DriverManager.cleanUp();
            }
            drivers.remove();
        }
    }

    @Override
    public abstract void initialize();

    public void configureWindowSize(WebDriver driver, boolean useMaxSize) {
        int width;
        int height;
        if (useMaxSize) {
            Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
            width = size.width;
            height = size.height;
        } else {
            String[] dimensions = PropertyResolver.getBrowserScreenSize().split(",");
            width = Integer.parseInt(dimensions[0]);
            height = Integer.parseInt(dimensions[1]);
        }
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(width, height));
        info("Used screen size width: " + width + " height: " + height);
    }
}
