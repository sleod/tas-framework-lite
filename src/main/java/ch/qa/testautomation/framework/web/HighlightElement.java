package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.error;

public class HighlightElement {
    public static synchronized void highlightElement(WebElement element, WebDriver driver, long sleepMilli, String color) {
        if (PropertyResolver.demoModeEnabled()) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: " + color + "; border: 3px solid " + color + ";");
            sleep(sleepMilli);
        }
    }

    public static synchronized void resetElement(WebElement element, WebDriver driver, long sleepMilli) {
        if (PropertyResolver.demoModeEnabled()) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");
            sleep(sleepMilli);
        }
    }

    private static synchronized void sleep(long millis) {
        try {
            Thread.currentThread().sleep(millis);
        } catch (InterruptedException e) {
            error(e);
        }
    }
}
