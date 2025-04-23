package ch.qa.testautomation.tas.web;

import ch.qa.testautomation.tas.exception.tasBaseException;
import ch.qa.testautomation.tas.exception.tasErrorKeys;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Getter
public class PlaywrightWebElement {

    private final Page page;
    private String searchCriteria;
    private Locator locator;

    public PlaywrightWebElement(Page page, String searchCriteria) {
        this.page = page;
        this.searchCriteria = searchCriteria;
    }

    public PlaywrightWebElement(Page page, Locator locator) {
        this.page = page;
        this.locator = locator;
    }

    /**
     * Try to locate element with defined locator
     *
     * @return element if found, else null
     */
    public Locator getElement() {
        if (exists()) {
            return locator;
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE,
                    "Element not found with search criteria: " + searchCriteria);
        }
    }

    public boolean exists() {
        if (locator == null) {
            try {
                locator = page.locator(searchCriteria);
                return locator != null;
            } catch (Throwable e) {
                return false;
            }
        } else {
            return true;
        }
    }

    public void click() {
        getElement().click();
    }

    public void doubleClick() {
        getElement().dblclick();
    }

    public void sendKeys(String text) {
        getElement().fill(text);
    }

    public void press(String key) {
        getElement().press(key);
    }

    public String text() {
        return getElement().textContent();
    }

    public String val() {
        return attr("value");
    }

    public boolean isDisplayed() {
        return getElement().isVisible();
    }

    public void scrollIntoView() {
        getElement().scrollIntoViewIfNeeded();
    }

    public Locator parent() {
        return getElement().locator("xpath=..");
    }

    public Locator ancestorXpath(String selector) {
        return getElement().locator("xpath=ancestor::" + selector);
    }

    public String attr(String name) {
        return getElement().getAttribute(name);
    }

    public void clear() {
        getElement().clear();
    }

    public void hover() {
        getElement().hover();
    }

    public PlaywrightWebElement find(String selector) {
        return new PlaywrightWebElement(getPage(), selector);
    }

    public void download(String filePath) {
        getPage().waitForDownload(() -> getElement().click()).saveAs(Paths.get(filePath));
    }

    public boolean hasAttribute(String attributeName) {
        return Objects.nonNull(attr(attributeName));
    }

    public int count() {
        return getElement().count();
    }

    public boolean isSingle() {
        return getElement().count() == 1;
    }

    public List<Locator> getAll() {
        return getElement().all();
    }

    private void flash(Locator locator) {
        Object originalBorder = locator.evaluate("el => el.style.border");
        locator.evaluate("el => el.style.border = '2px solid red'");
        page.waitForTimeout(300);
        String border = Objects.nonNull(originalBorder) ? originalBorder.toString() : "";
        locator.evaluate("el => el.style.border = arguments[0]", border);
        page.waitForTimeout(300);

    }

}