package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.media.ImageHandler;
import org.openqa.selenium.By;
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
        HighlightElement.highlightElement(element, driver, PropertyResolver.getDemoModeSleep(), PropertyResolver.getDemoModeHighLightColor());
        ImageHandler.handleImage(driver);
        HighlightElement.resetElement(element, driver, PropertyResolver.getDemoModeSleep());
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
        ImageHandler.handleImage(driver);
    }

}