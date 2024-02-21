package ch.qa.testautomation.tas.web;

import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.media.ImageHandler;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import java.lang.reflect.Method;

public class WebDriverEventCapture implements WebDriverListener {

    /**
     * perform action after find any element
     *
     * @param driver  web driver
     * @param by      selector
     * @param element element
     */
    @Override
    public void afterFindElement(WebDriver driver, By by, WebElement element) {
        if (PropertyResolver.isDemoModeEnabled()) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String style = "";
            if (element.getAttribute("style") != null) {
                style = element.getAttribute("style");
            }
            js.executeScript("arguments[0].style.border='2px solid red'", element);
            sleep300();
            ImageHandler.saveScreenshot(driver);
            js.executeScript("arguments[0].setAttribute('style', arguments[1])", element, style);
        }
    }

    private void sleep300() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Sleep interrupted!");
        }
    }

    /**
     * perform action after any web driver call to record screen for video
     *
     * @param driver web driver
     * @param method method called
     * @param args   args
     * @param result result
     */
    @Override
    public void afterAnyWebDriverCall(WebDriver driver, Method method, Object[] args, Object result) {
        ImageHandler.saveScreenshot(driver);
    }
}
