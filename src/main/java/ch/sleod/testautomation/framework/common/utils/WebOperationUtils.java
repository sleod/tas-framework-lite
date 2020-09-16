package ch.sleod.testautomation.framework.common.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.ArrayList;

public class WebOperationUtils {

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
     * wait until the target web element shows with time out for 10 secs
     *
     * @param driver     web driver
     * @param webElement the Web Element
     * @return the Web Element
     */
    public static WebElement waitUntilVisible(WebDriver driver, WebElement webElement) {
        return (new WebDriverWait(driver, 10)).until(ExpectedConditions.visibilityOf(webElement));
    }

    /**
     * wait until the target web element disappear with time out for 10 secs
     *
     * @param driver     web driver
     * @param webElement the Web Element
     * @return true if the element disappeared
     */
    public static boolean waitUntilDisappear(WebDriver driver, WebElement webElement) {
        return (new WebDriverWait(driver, 10)).until(ExpectedConditions.invisibilityOf(webElement));
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
     * default wait method for loading page, mostly useful when testing pages with little dynamic content
     *
     * @param driver driver
     * @return true case the page with body showed
     */

    /**
     * wait for page load with timeout for 10 secs
     *
     * @param driver web driver
     * @return true if page loaded
     */
    public static boolean waitForPageLoad(WebDriver driver) {
        Wait<WebDriver> wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("Body")));
        return true;
    }

    /**
     * Wait for page load with element
     *
     * @param driver   driver
     * @param selector selector with by (e.g. By.xpath(), By.id()...)
     * @return WebElement expected element
     */
    public static WebElement waitForElementLoad(WebDriver driver, By selector) {
        Wait<WebDriver> wait = new WebDriverWait(driver, 10);
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
     * wait until element is displayed
     *
     * @param driver     driver
     * @param webElement webElement
     * @param timeout    long
     * @author Andrej Bagoutdinov
     */
    public static void waitForElementIsDisplayed(WebDriver driver, WebElement webElement, long timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        //isDisplayed() is deprecated
        //ExpectedCondition<Boolean> elementIsDisplayed = arg0 -> webElement.isDisplayed();
        wait.until(ExpectedConditions.visibilityOf(webElement));
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

}
