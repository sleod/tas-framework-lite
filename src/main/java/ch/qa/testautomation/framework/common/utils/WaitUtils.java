package ch.qa.testautomation.framework.common.utils;

import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

public class WaitUtils {

    private static int timeout = 10;

    public static void setTimeout(int timeout) {
        WaitUtils.timeout = timeout;
    }

    /**
     * Wait until element is visible
     *
     * @param selenideElement element to check
     * @param duration        time to wait
     */
    public static SelenideElement waitUntilVisible(SelenideElement selenideElement, int duration) {
        return selenideElement.should(visible, Duration.ofSeconds(duration));
    }

    /**
     * Wait until element is visible
     *
     * @param selenideElement element to check
     */
    public static SelenideElement waitUntilVisible(SelenideElement selenideElement) {
        return selenideElement.should(visible);
    }

    /**
     * Waits the defined time of selenide to appear
     *
     * @param selector selector with by (e.g. By.xpath(), By.id()...)
     * @return WebElement expected element
     */
    public static SelenideElement waitUntilVisible(By selector) {
        return $(selector).should(visible);
    }

    public static SelenideElement waitUntilVisible(By selector, int duration) {
        return $(selector).should(visible, Duration.ofSeconds(duration));
    }

    public static SelenideElement waitUntilVisibleExpandedTimeout(By selector) {
        return $(selector).should(visible, Duration.ofSeconds(timeout));
    }

    /**
     * Wait until element is visible, use timeout value from WaitUtil variable
     *
     * @param selenideElement element to check
     * @return SelenideElement
     */
    public static SelenideElement waitUntilVisibleExpandedTimeout(SelenideElement selenideElement) {
        return waitUntilVisible(selenideElement, timeout);
    }

    /**
     * Wait until the target web elements text is visible
     *
     * @param selenideElement element to check
     */
    public static SelenideElement waitUntilExactTextIsVisible(SelenideElement selenideElement, String textToWait) {
        return selenideElement.shouldHave(exactText(textToWait));
    }

    /**
     * Wait until the target web elements text is visible with own timeout
     *
     * @param selenideElement element to check
     */
    public static SelenideElement waitUntilExactTextIsVisible(SelenideElement selenideElement, String textToWait, int duration) {
        return selenideElement.shouldHave(exactText(textToWait), Duration.ofSeconds(duration));
    }

    /**
     * Wait 10 sec until the target web elements text is visible
     *
     * @param selenideElement element to check
     */
    public static SelenideElement waitUntilExactTextIsVisibleExpandedTimeout(SelenideElement selenideElement, String textToWait) {
        return waitUntilExactTextIsVisible(selenideElement, textToWait, timeout);
    }

    /**
     * Waits of selenide to disappear
     *
     * @param webElement the SelenideElement Web Element
     */
    public static SelenideElement waitUntilDisappear(SelenideElement webElement) {
        return webElement.should(hidden);
    }

    /**
     * Waits the defined time of selenide to disappear
     *
     * @param webElement the SelenideElement Web Element
     */
    public static SelenideElement waitUntilDisappear(SelenideElement webElement, int duration) {
        return webElement.should(hidden, Duration.ofSeconds(duration));
    }

    /**
     * Waits the defined time of selenide to disappear
     *
     * @param webElement the SelenideElement Web Element
     */
    public static SelenideElement waitUntilDisappearExpandedTimeout(SelenideElement webElement) {
        return webElement.should(hidden, Duration.ofSeconds(timeout));
    }

    public static void waitStep(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            throw new ApollonBaseException(ApollonErrorKeys.INTERRUPTED_WHILE_WAITING, ex);
        }
    }

    public static SelenideElement waitForPageLoad() {
        return waitUntilVisible(By.tagName("Body"));
    }

    public static SelenideElement waitForPageLoad(int timeout) {
        return waitUntilVisible(By.tagName("Body"), timeout);
    }

    /**
     * Wait for page load with timeout for 10 secs
     * *
     *
     * @return true if page loaded
     */
    public static SelenideElement waitForPageLoadExpandedTimeout() {
        return waitUntilVisible(By.tagName("Body"), timeout);
    }

    /**
     * default wait method for loading page when page uses dynamic content heavily or in a slow environment
     *
     * @param duration in seconds / int
     * @return true case the page with body showed
     */
    public static SelenideElement waitForPageLoadToBeClickable(SelenideElement selenideElement, int duration) {

        return selenideElement.should(visible).should(enabled, Duration.ofSeconds(duration));
    }

    /**
     * fluent wait with timeout and polling period until the element is visible
     *
     * @param driver         web driver
     * @param element        web element to wait
     * @param secondsTimeout time out in sec
     * @param secondsPolling polling period in sec
     */
    public static void fluentWaitForElementWithPolling(WebDriver driver, WebElement element, int secondsTimeout, int secondsPolling) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(secondsTimeout))
                .pollingEvery(Duration.ofSeconds(secondsPolling))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class);
        wait.until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * fluent wait with timeout and polling period until the element is visible
     *
     * @param driver         web driver
     * @param by             selector of web element to wait
     * @param secondsTimeout time out in sec
     * @param secondsPolling polling period in sec
     */
    public static void fluentWaitForVisibilityOfElementLocated(WebDriver driver, By by, int secondsTimeout, int secondsPolling) {
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(secondsTimeout))
                .pollingEvery(Duration.ofSeconds(secondsPolling))
                .ignoring(NoSuchElementException.class)
                .ignoring(StaleElementReferenceException.class)
                .ignoring(ElementClickInterceptedException.class);
        wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }
}