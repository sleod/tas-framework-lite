package ch.sleod.testautomation.google;

import ch.sleod.testautomation.framework.common.utils.WebOperationUtils;
import ch.sleod.testautomation.framework.core.annotations.TestObject;
import ch.sleod.testautomation.framework.core.annotations.TestStep;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.Map;

import static org.junit.Assert.assertTrue;

@TestObject(name = "Search Result Page")
public class SearchResultPage extends FunctionPage {

    @FindBy(tagName = "h3")
    private WebElement firstEntry;

    @TestStep(name = "Check First Entry of Search Result", using = "searchResultEntry", takeScreenshot = true)
    public void firstEntryOfSearchResultContains(String text) {
        String firstEntryText = WebOperationUtils.waitUntilVisible(driver, firstEntry).getText();
        assertTrue("The first Entry of Search ist not about: " + firstEntryText + " <-> " + text, firstEntryText.contains(text));
    }

    @Override
    @TestStep(name = "Check Fields Present")
    public void checkFields() {
        assertTrue("Input Field is not displayed!", searchInputField.isDisplayed());
        assertTrue("Result Entry is not displayed!", firstEntry.isDisplayed());
    }

    @TestStep(name = "Check search with map", using = "searchTextMap")
    public void checkSearchs(Map<String, Object> texts) {
        texts.forEach((key, value) -> {
            searchText(key);
            firstEntryOfSearchResultContains(String.valueOf(value));
        });
    }

}
