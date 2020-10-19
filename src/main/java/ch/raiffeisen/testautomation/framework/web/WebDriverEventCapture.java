package ch.raiffeisen.testautomation.framework.web;

import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import ch.raiffeisen.testautomation.framework.core.media.ImageHandler;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

public class WebDriverEventCapture implements WebDriverEventListener {

    WebElement target = null;

    @Override
    public void beforeAlertAccept(WebDriver driver) {
    }

    @Override
    public void afterAlertAccept(WebDriver driver) {
        ImageHandler.handleImage(driver);
    }

    @Override
    public void afterAlertDismiss(WebDriver driver) {
        ImageHandler.handleImage(driver);
    }

    @Override
    public void beforeAlertDismiss(WebDriver driver) {
    }

    @Override
    public void beforeNavigateTo(String url, WebDriver driver) {

    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {

    }

    @Override
    public void beforeNavigateBack(WebDriver driver) {

    }

    @Override
    public void afterNavigateBack(WebDriver driver) {

    }

    @Override
    public void beforeNavigateForward(WebDriver driver) {
    }

    @Override
    public void afterNavigateForward(WebDriver driver) {
        ImageHandler.handleImage(driver);
    }

    @Override
    public void beforeNavigateRefresh(WebDriver driver) {
    }

    @Override
    public void afterNavigateRefresh(WebDriver driver) {
    }

    @Override
    public void beforeFindBy(By by, WebElement element, WebDriver driver) {
    }

    @Override
    public void afterFindBy(By by, WebElement element, WebDriver driver) {
        if(element instanceof RemoteWebElement) {
            if (target == null || !target.equals(element)) {
                target = element;
                HighlightElement.highlightElement(element, driver, PropertyResolver.getDemoModeSleep(), PropertyResolver.getDemoModeHighLightColor());
                ImageHandler.handleImage(driver);
                HighlightElement.resetElement(element, driver, PropertyResolver.getDemoModeSleep());
            }
        }
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        ImageHandler.handleImage(driver);
    }

    @Override
    public void beforeChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
    }

    @Override
    public void afterChangeValueOf(WebElement element, WebDriver driver, CharSequence[] keysToSend) {
        ImageHandler.handleImage(driver);
    }

    @Override
    public void beforeScript(String script, WebDriver driver) {
    }

    @Override
    public void afterScript(String script, WebDriver driver) {
    }

    @Override
    public void beforeSwitchToWindow(String windowName, WebDriver driver) {

    }

    @Override
    public void afterSwitchToWindow(String windowName, WebDriver driver) {
    }

    @Override
    public void onException(Throwable throwable, WebDriver driver) {

    }

    @Override
    public <X> void beforeGetScreenshotAs(OutputType<X> target) {

    }

    @Override
    public <X> void afterGetScreenshotAs(OutputType<X> target, X screenshot) {

    }

    @Override
    public void beforeGetText(WebElement element, WebDriver driver) {
    }

    @Override
    public void afterGetText(WebElement element, WebDriver driver, String text) {
    }
}
