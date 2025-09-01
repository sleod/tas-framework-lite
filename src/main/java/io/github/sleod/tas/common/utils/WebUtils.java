package io.github.sleod.tas.common.utils;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Selenide.$;
import static io.github.sleod.tas.common.logging.SystemLogger.info;
import static io.github.sleod.tas.common.utils.StringTextUtils.isValid;

/**
 * Utility class for web-related operations.
 */
public class WebUtils {

    public static final String BLOCK_CENTER = "{block: \"center\"}";
    public static final String BLOCK_END = "{block: \"end\"}";

    /**
     * scroll element to middle of view
     *
     * @param target element to be scrolled to middle of view
     */
    public static void scrollToMiddleOfView(WebElement target) {
        String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                                         + "var elementTop = arguments[0].getBoundingClientRect().top;"
                                         + "window.scrollBy(0, elementTop-(viewPortHeight/2));";
        Selenide.executeJavaScript(scrollElementIntoMiddle, target);
    }

    public static void scrollIntoViewCenter(SelenideElement element) {
        element.scrollIntoView(BLOCK_CENTER);
    }

    /**
     * Mask a value with a single quote to use in a XPath
     */
    public static String parseSingleQuote(String valueWithSingleQuote) {
        return "\"" + valueWithSingleQuote + "\"";
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
     * Checks whether the element has the exact text
     */
    public static SelenideElement existsExactTextInElement(SelenideElement elementContainer, String valueToCheck) {
        return elementContainer.find(byText(valueToCheck)).should(Condition.exist);
    }

    /**
     * Checks whether the element contains the text
     */
    public static SelenideElement existsContainsTextInElement(SelenideElement elementContainer, String valueToCheck) {
        return elementContainer.find(withText(valueToCheck)).should(Condition.exist);
    }

    /**
     * Find the element with text
     */
    public static SelenideElement findElementWithText(String elementWithText) {
        return $(byText(elementWithText));
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
        ArrayList<String> tabs2 = new ArrayList<>(driver.getWindowHandles());
        try {
            driver.switchTo().window(tabs2.get(index));
        } catch (IndexOutOfBoundsException ex) {
            info("Switch Tab with index: " + index + " failed!");
        }
    }

    /**
     * switch to iframe via name
     *
     * @param iframeName name of the iframe
     */
    public static void switchToIframeWithName(String iframeName) {
        Selenide.switchTo().frame(iframeName);
    }

    /**
     * switch to default iframe
     */
    public static void switchToDefaultIframe() {
        Selenide.switchTo().defaultContent();
    }

    /**
     * default click on background of page
     */
    public static void clickOnBackGround() {
        Selenide.$(By.tagName("Body")).click();
    }

    /**
     * default javascript hover that moves mouse over specified webElement
     *
     * @param webElement webElement
     */
    public static void moveToElement(WebElement webElement) {
        Selenide.actions().moveToElement(webElement).perform();
    }

    /**
     * wait specified amount of time until url contains a certain text (useful for long loads or redirections)
     *
     * @param driver     driver
     * @param timeout    long
     * @param partOfText String
     */
    public static void checkIfUrlContains(WebDriver driver, long timeout, String partOfText) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        ExpectedCondition<Boolean> urlIsCorrect = arg0 -> driver.getCurrentUrl().contains(partOfText);
        wait.until(urlIsCorrect);
    }

    /**
     * send value to field if not null and not empty
     *
     * @param value value
     * @param field target field
     */
    public void sendKeysToField(String value, SelenideElement field) {
        if (isValid(value)) {
            field.should(Condition.appear).sendKeys(value);
        }
    }


    /**
     * find element in shadow dom with query selector per javaScript
     *
     * @param selectors css selectors
     * @return WebElement
     */
    public static SelenideElement getShadowElement(String... selectors) {
        String jsCode = buildSelectorInJS(selectors);
        SelenideElement element = Selenide.executeJavaScript(jsCode);
        if (element != null) {
            return element;
        } else {
            throw new RuntimeException("Element was not found with such JS: " + jsCode);
        }
    }

    /**
     * find element in shadow dom after the element with query selector per javaScript
     *
     * @param target     to shadow dom host element
     * @param shadowHost css selector
     * @return WebElement
     */
    public static SelenideElement getShadowElementAfter(String target, String shadowHost) {
        return Selenide.$(shadowCss(target, shadowHost)).should(Condition.appear);
    }

    /**
     * find element in shadow dom after the element with query selector
     *
     * @param field       shadow dom host element
     * @param cssSelector css selector
     * @return WebElement
     */
    public static WebElement getShadowElementAfter(SelenideElement field, String cssSelector) {
        return field.should(Condition.appear).getShadowRoot().findElement(By.cssSelector(cssSelector));
    }

    /**
     * find all elements via selectors crossing shadow dom
     *
     * @return list of elements
     */
    public static List<SelenideElement> getAllShadowElements(String target, String... shadowHosts) {
        if (shadowHosts.length == 1) {
            return Selenide.$$(shadowCss(target, shadowHosts[0])).asDynamicIterable().stream().toList();
        } else if (shadowHosts.length == 2) {
            return Selenide.$$(shadowCss(target, shadowHosts[0], shadowHosts[1])).asDynamicIterable().stream().toList();
        } else if (shadowHosts.length > 2) {
            return Selenide.$$(shadowCss(target, shadowHosts[0], Arrays.copyOfRange(shadowHosts, 1, shadowHosts.length - 1))).asDynamicIterable().stream().toList();
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Shadow Host Selector Array is empty!");
        }
    }

    /**
     * find element via text in shadow dom with query selector per javaScript
     *
     * @param text      text in element
     * @param selectors css selectors
     * @return element
     */
    public static SelenideElement findShadowElementWithText(String text, String target, String... selectors) {
        List<SelenideElement> result = getAllShadowElements(target, selectors)
                .stream().filter(ele -> ele.text().equals(text)).toList();
        if (result.size() > 0) {
            if (result.size() > 1) {
                info("with the text: " + text + " more than one element was found!");
            }
            return result.get(0);
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Element with text: " + text + " was not found!");
        }
    }

    /**
     * Scrolls the given element horizontally to the right.
     * @param element the element to scroll
     */
    public static void scrollHorizontalToRight(WebElement element) {
        Selenide.executeJavaScript("arguments[0].scrollLeft += arguments[0].offsetWidth", element);
    }

    /**
     * Scrolls the given element horizontally to the left.
     * @param element the element to scroll
     */
    public static void scrollHorizontalToLeft(WebElement element) {
        Selenide.executeJavaScript("arguments[0].scrollLeft = 0", element);
    }

    /**
     * Prints the contents of a string map.
     * @param stringMap the string map to print
     * @return a formatted string representation of the map
     */
    public static String printStringMap(Map<String, String> stringMap) {
        return " | " + stringMap.keySet().stream().map(stringMap::get).collect(Collectors.joining(" | ")) + " | ";
    }

    /**
     * Gets the current tab index in the browser.
     * @param driver the WebDriver instance
     * @return the index of the current tab
     */
    public int getCurrentTabIndex(WebDriver driver) {
        String currentTab = driver.getWindowHandle();
        ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
        return tabs.indexOf(currentTab);
    }

    /**
     * Builds a JavaScript selector string for the given CSS selectors.
     * @param selectors the CSS selectors
     * @return the JavaScript selector string
     */
    private static String buildSelectorInJS(String... selectors) {
        StringBuilder js = new StringBuilder();
        js.append("return document.querySelector(\"");
        for (int i = 0; i < selectors.length; i++) {
            String key = selectors[i];
            js.append(key).append("\")");
            if (i < selectors.length - 1) {
                js.append(".shadowRoot.querySelector(\"");
            }
        }
        return js.toString();
    }

}