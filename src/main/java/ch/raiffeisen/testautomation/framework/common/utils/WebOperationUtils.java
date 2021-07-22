package ch.raiffeisen.testautomation.framework.common.utils;

import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;

public class WebOperationUtils {

    private static int timeout = 10;

    public static void setTimeout(int timeout) {
        WebOperationUtils.timeout = timeout;
    }

    /**
     * scroll element to middle of view
     *
     * @param driver web driver
     * @param target element to be scroll to middle of view
     */
    public static void scrollToMiddleOfView(WebDriver driver, WebElement target) {
        JavascriptExecutor js = ((JavascriptExecutor) driver);
        String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                + "var elementTop = arguments[0].getBoundingClientRect().top;"
                + "window.scrollBy(0, elementTop-(viewPortHeight/2));";
        js.executeScript(scrollElementIntoMiddle, target);
    }

    /**
     * check if the web element in view port of screen
     *
     * @param driver  web driver
     * @param element web element
     * @return true if the web element in view port of screen
     */
    public static Boolean isVisibleInViewport(WebDriver driver, WebElement element) {
        return (Boolean) ((JavascriptExecutor) driver).executeScript(
                "var elem = arguments[0],                 " +
                        "  box = elem.getBoundingClientRect(),    " +
                        "  cx = box.left + box.width / 2,         " +
                        "  cy = box.top + box.height / 2,         " +
                        "  e = document.elementFromPoint(cx, cy); " +
                        "for (; e; e = e.parentElement) {         " +
                        "  if (e === elem)                        " +
                        "    return true;                         " +
                        "}                                        " +
                        "return false;                            "
                , element);
    }

    /**
     * Wait until the target web element shows with timeout for 10 secs
     *
     * @param driver     web driver
     * @param webElement the Web Element
     * @return the Web Element
     */
    public static WebElement waitUntilVisible(WebDriver driver, WebElement webElement) {
        return waitUntilVisibleTimeout(driver, webElement, timeout);
    }

    /**
     * Wait until the target web elements text is visible with timeout for 10 secs
     *
     * @param driver     web driver
     * @param webElement the Web Element
     * @return the Web Element
     */
    public static void waitUntilTextIsVisible(WebDriver driver, WebElement webElement, String textToWait) {
        waitUntilTextIsVisible(driver, webElement, textToWait, 10);
    }

    /**
     * Wait until the target web elements text is visible with own timeout
     *
     * @param driver     web driver
     * @param webElement the Web Element
     * @return the Web Element
     */
    public static void waitUntilTextIsVisible(WebDriver driver, WebElement webElement, String textToWait, long timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.textToBePresentInElement(webElement, textToWait));
    }

    /**
     * wait until element is displayed
     *
     * @param driver     driver
     * @param webElement webElement
     * @param timeout    long
     * @author Andrej Bagoutdinov
     */
    public static WebElement waitUntilVisibleTimeout(WebDriver driver, WebElement webElement, long timeout) {
        return (new WebDriverWait(driver, timeout)).until(ExpectedConditions.visibilityOf(webElement));
    }

    /**
     * Waits the defined time of selenide
     *
     * @param selenideElement Selenide Element
     * @return SelenideElement
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

    /**
     * Waits the defined time of selenide to appear
     *
     * @param selector selector with by (e.g. By.xpath(), By.id()...)
     * @return WebElement expected element
     * @deprecated Use waitUntilVisible(By selector) Since 1.0.6.05
     */
    @Deprecated
    public static SelenideElement waitForElementLoad(By selector) {
        return $(selector).should(visible);
    }

    /**
     * wait until the target web element disappear with time out for 10 secs
     *
     * @param driver     web driver
     * @param webElement the SelenideElement Web Element
     * @return true if the element disappeared
     */
    public static boolean waitUntilDisappear(WebDriver driver, WebElement webElement) {
        return (new WebDriverWait(driver, timeout)).until(ExpectedConditions.invisibilityOf(webElement));
    }

    /**
     * Waits the defined time of selenide to disappear
     *
     * @param webElement the SelenideElement Web Element
     */
    public static void waitUntilDisappear(SelenideElement webElement) {
        webElement.should(hidden);
    }


    /**
     * accept the primary alert
     *
     * @param driver web driver
     */
    public static void acceptAlert(WebDriver driver) {
        driver.switchTo().alert().accept();
    }

    /**
     * switch to tab via index
     *
     * @param driver web driver
     * @param index  tab index
     */
    public static void switchToTab(WebDriver driver, int index) {
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(index));
    }

    /**
     * switch to iframe via name
     *
     * @param driver     web driver
     * @param iframeName name of the iframe
     * @author Andrej Bagoutdinov
     */
    public static void switchToIframeWithName(WebDriver driver, String iframeName) {
        driver.switchTo().frame(iframeName);
    }

    /**
     * switch to default iframe
     *
     * @param driver web driver
     * @author Andrej Bagoutdinov
     */

    public static void switchToDefaultIframe(WebDriver driver) {
        driver.switchTo().defaultContent();
    }

    /**
     * Wait for page load with Selenide Timeout
     */
    public static void waitForPageLoad() {
        $("body").should(visible);
    }

    /**
     * Wait for page load with timeout for 10 secs
     *
     * @param driver web driver
     * @return true if page loaded
     */
    public static boolean waitForPageLoad(WebDriver driver) {
        Wait<WebDriver> wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("Body")));
        return true;
    }

    /**
     * Wait for page load with element with timeout for 10 secs
     *
     * @param driver   driver
     * @param selector selector with by (e.g. By.xpath(), By.id()...)
     * @return WebElement expected element
     */
    public static WebElement waitForElementLoad(WebDriver driver, By selector) {
        Wait<WebDriver> wait = new WebDriverWait(driver, timeout);
        wait.until(ExpectedConditions.presenceOfElementLocated(selector));
        return driver.findElement(selector);
    }

    /**
     * default wait method for loading page when page uses dynamic content heavily or in a slow environment
     *
     * @param driver     driver
     * @param webElement WebElement
     * @param timeOut    in seconds / int
     * @return true case the page with body showed
     */
    public static WebElement waitForPageLoadByClickability(WebDriver driver, WebElement webElement, int timeOut) {
        return (new WebDriverWait(driver, timeOut)).until(ExpectedConditions.elementToBeClickable(webElement));
    }

    /**
     * calls a page checks title and retries; useful when publishing a page and waiting for the publish to be completed.
     *
     * @param driver          Webdriver
     * @param url             The Url you want to call repeatedly
     * @param refreshAttempts the amount of attempts that constitute the timeout together with the refreshInterval
     * @param refreshInterval in milliseconds
     * @param expectedTitle   the string that contains the expected title
     * @return boolean match if titles match that can be used in assertion
     * @author Andrej Bagoutdinov
     */
    public static boolean waitForPageLoadAndRetry(WebDriver driver, String url, int refreshAttempts, long refreshInterval, String expectedTitle) {
        int attempts = 0;
        driver.get(url);
        WebOperationUtils.waitForPageLoad(driver);
        String actualTitle = driver.getTitle();

        while (!expectedTitle.equals(actualTitle) && attempts < refreshAttempts) {
            driver.get(url);
            WebOperationUtils.waitForPageLoad(driver);
            actualTitle = driver.getTitle();
            attempts++;
            try {
                Thread.sleep(refreshInterval);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
        return actualTitle.equals(expectedTitle);
    }

    /**
     * default click on back ground of page
     *
     * @param driver driver
     */
    public static void clickOnBackGround(WebDriver driver) {
        driver.findElement(By.tagName("Body")).click();
    }

    /**
     * default javascript hover that moves mouse over specified webElement
     *
     * @param driver     driver
     * @param webElement webElement
     * @author Andrej Bagoutdinov
     */
    public static void hoverOverElementWithJs(WebDriver driver, WebElement webElement) {
        Actions builder = new Actions(driver);
        builder.moveToElement(webElement).perform();
    }

    /**
     * wait specified amount of time until url contains a certain text (useful for long loads or redirections)
     *
     * @param driver     driver
     * @param timeout    long
     * @param partOfText String
     * @author Andrej Bagoutdinov
     */
    public static void checkIfUrlContains(WebDriver driver, long timeout, String partOfText) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        ExpectedCondition<Boolean> urlIsCorrect = arg0 -> driver.getCurrentUrl().contains(partOfText);
        wait.until(urlIsCorrect);
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

    public static void waitStep(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (Exception ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while webdriver waiting...\n" + ex.getMessage());
        }
    }
}