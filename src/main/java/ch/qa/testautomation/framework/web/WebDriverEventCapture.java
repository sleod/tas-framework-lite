package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.media.ImageHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

import java.lang.reflect.Method;

public class WebDriverEventCapture implements WebDriverListener {

    @Override
    public void afterFindElement(WebDriver driver, By by, WebElement element) {
        HighlightElement.highlightElement(element, driver, PropertyResolver.getDemoModeSleep(), PropertyResolver.getDemoModeHighLightColor());
        ImageHandler.handleImage(driver);
        HighlightElement.resetElement(element, driver, PropertyResolver.getDemoModeSleep());
    }

    @Override
    public void afterAnyWebDriverCall(WebDriver driver, Method method, Object[] args, Object result) {
        ImageHandler.handleImage(driver);
    }

}
