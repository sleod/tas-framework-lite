package ch.qa.testautomation.tas.web;

import ch.qa.testautomation.tas.common.abstraction.SingleTestObject;
import ch.qa.testautomation.tas.core.annotations.SearchCriteria;
import ch.qa.testautomation.tas.core.component.DriverManager;
import ch.qa.testautomation.tas.exception.tasBaseException;
import ch.qa.testautomation.tas.exception.tasErrorKeys;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class PlaywrightPage extends SingleTestObject {

    @Getter
    private final Page page;

    public PlaywrightPage() {
        this.page = DriverManager.getPlaywrightDriver().getPage();
        initElements();
    }

    public void initElements() {
        for (Field field : this.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(SearchCriteria.class)) {
                SearchCriteria findBy = field.getAnnotation(SearchCriteria.class);
                field.setAccessible(true);
                try {
                    field.set(this, new PlaywrightWebElement(getPage(), findBy.locator()));
                } catch (IllegalAccessException ex) {
                    throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Failed to initialize element: " + field.getName());
                }
            }
        }
    }

    public void close() {
        getPage().close();
    }

    public void open(String url) {
        getPage().navigate(url);
    }

    public PlaywrightWebElement getByPlaceholder(String placeholder) {
        return new PlaywrightWebElement(getPage(), getPage().getByPlaceholder(placeholder));
    }

    public PlaywrightWebElement getByLabel(String label) {
        return new PlaywrightWebElement(getPage(), getPage().getByLabel(label));
    }

    public PlaywrightWebElement getByText(String text) {
        return new PlaywrightWebElement(getPage(), getPage().getByText(text));
    }

    public PlaywrightWebElement getByText(String tagName, String text) {
        return new PlaywrightWebElement(getPage(), getPage().locator(tagName, new Page.LocatorOptions().setHasText(text)));
    }

    public PlaywrightWebElement getByTitle(String title) {
        return new PlaywrightWebElement(getPage(), getPage().getByTitle(title));
    }

    public PlaywrightWebElement getByTextCaseInsensitive(String pattern) {
        return new PlaywrightWebElement(getPage(), getPage().getByText(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)));
    }

    public PlaywrightWebElement getByRole(AriaRole role, String name) {
        return new PlaywrightWebElement(getPage(), getPage().getByRole(role, new Page.GetByRoleOptions().setName(name)));
    }

    public PlaywrightWebElement getLink(String text) {
        return getByRole(AriaRole.LINK, text);
    }

    public PlaywrightWebElement getByTestId(String testId) {
        return new PlaywrightWebElement(getPage(), getPage().getByTestId(testId));
    }

    public PlaywrightWebElement find(String selector) {
        return new PlaywrightWebElement(getPage(), selector);
    }

    public void waitForSec(int seconds) {
        getPage().waitForTimeout(seconds * 1000);
    }

    public void waitForMilliSec(int milliSec) {
        getPage().waitForTimeout(milliSec);
    }

}
