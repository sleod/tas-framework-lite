package example.google;

import ch.qa.testautomation.framework.core.annotations.TestObject;
import ch.qa.testautomation.framework.core.annotations.TestStep;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.support.FindBy;


import java.util.Map;

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static org.junit.Assert.assertTrue;

@TestObject(name = "Search Result Page")
public class SearchResultPage extends FunctionPage {

    @FindBy(tagName = "h3")
    private SelenideElement firstEntry;

    @TestStep(name = "Check First Entry of Search Result", using = "searchResultEntry",takeScreenshot = true)
    public void firstEntryOfSearchResultContains(String text) {
        String firstEntryText = firstEntry.getText();
        boolean contain = containsIgnoreCase(firstEntryText,text);
        assertTrue("The first Entry of Search ist not about: " + firstEntryText + " <-> " + text, contain);
        logStepInfo("Check first entry of search result!");
    }

    @Override
    @TestStep(name = "Check Fields Present")
    public void checkFields() {
        assertTrue("Input Field is not displayed!", searchInputField.isDisplayed());
        assertTrue("Result Entry is not displayed!", firstEntry.isDisplayed());
    }

    @TestStep(name = "Check search with map", using = "searchTextMap",takeScreenshot = true)
    public void checkSearchs(Map<String, Object> texts) {
        texts.forEach((key, value) -> {
            searchText(key);
            firstEntryOfSearchResultContains(String.valueOf(value));
        });
    }

}
