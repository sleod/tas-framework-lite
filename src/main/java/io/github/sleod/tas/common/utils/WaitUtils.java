package io.github.sleod.tas.common.utils;

import com.codeborne.selenide.SelenideElement;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

/**
 * Utility class for waiting operations.
 */
public class WaitUtils {

    private static int timeout = 10;


    /**
     * Sets the default timeout value (in seconds) for wait operations.
     *
     * @param timeout the timeout in seconds
     */
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
        return selenideElement.should(appear, Duration.ofSeconds(duration));
    }

    /**
     * Wait until element is visible
     *
     * @param selenideElement element to check
     */
    public static SelenideElement waitUntilVisible(SelenideElement selenideElement) {
        return selenideElement.should(appear);
    }

    /**
     * Waits the defined time of selenide to appear
     *
     * @param selector selector with by (e.g. By.xpath(), By.id()...)
     * @return WebElement expected element
     */
    public static SelenideElement waitUntilVisible(By selector) {
        return $(selector).should(appear);
    }


    /**
     * Wait until element located by selector is visible for a given duration.
     *
     * @param selector selector with by (e.g. By.xpath(), By.id()...)
     * @param duration time to wait in seconds
     * @return SelenideElement expected element
     */
    public static SelenideElement waitUntilVisible(By selector, int duration) {
        return $(selector).should(appear, Duration.ofSeconds(duration));
    }


    /**
     * Wait until element located by selector is visible using the expanded timeout value.
     *
     * @param selector selector with by (e.g. By.xpath(), By.id()...)
     * @return SelenideElement expected element
     */
    public static SelenideElement waitUntilVisibleExpandedTimeout(By selector) {
        return $(selector).should(appear, Duration.ofSeconds(timeout));
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


    /**
     * Waits for the specified number of seconds (thread sleep).
     *
     * @param sec seconds to wait
     */
    public static void waitStep(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            throw new ExceptionBase(ExceptionErrorKeys.INTERRUPTED_WHILE_WAITING, ex);
        }
    }


    /**
     * Waits for the specified number of milliseconds (thread sleep).
     *
     * @param ms milliseconds to wait
     */
    public static void waitStepMilli(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            throw new ExceptionBase(ExceptionErrorKeys.INTERRUPTED_WHILE_WAITING, ex);
        }
    }


    /**
     * Waits for the page to load by waiting for the &lt;body&gt; tag to be visible.
     *
     * @return SelenideElement representing the body tag
     */
    public static SelenideElement waitForPageLoad() {
        return waitUntilVisible(By.tagName("Body"));
    }


    /**
     * Waits for the page to load by waiting for the &lt;body&gt; tag to be visible with a custom timeout.
     *
     * @param timeout time to wait in seconds
     * @return SelenideElement representing the body tag
     */
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

        return selenideElement.should(appear).should(enabled, Duration.ofSeconds(duration));
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