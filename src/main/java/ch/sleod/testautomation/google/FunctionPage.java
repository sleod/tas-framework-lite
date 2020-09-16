package ch.sleod.testautomation.google;

import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.annotations.TestStep;
import ch.sleod.testautomation.framework.web.WebPageObject;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class FunctionPage extends WebPageObject {

    @FindBy(name = "q")
    protected WebElement searchInputField;

    @Override
    @TestStep(name = "Check Fields Present")
    public void checkFields(){
        assertTrue("Input Field is not displayed!", searchInputField.isDisplayed());
    }

    @TestStep(name = "Start Up")
    public void startUp() {
        assertNotNull("Start URL is Null!",PropertyResolver.getProperty("startURL"));
        driver.get(PropertyResolver.getProperty("startURL"));
    }

    /**
     * enter search text
     *
     * @param text for search
     */
    @TestStep(name = "Search Text", using = "searchText", takeScreenshot = true)
    public void searchText(String text) {
        searchInputField.clear();
        logStepInfo("Test 1 ************");
        searchInputField.sendKeys(text);
        logStepInfo("Test 2 ************");
        searchInputField.sendKeys(Keys.ENTER);
        logStepInfo("Test 3 ************");
    }

    @TestStep(name = "Search Text Twice", using = "[searchText1, searchText2]")
    public void searchTwice(String text1, String text2) {
        searchInputField.clear();
        searchInputField.sendKeys(text1);
        searchInputField.sendKeys(Keys.ENTER);
        searchInputField.clear();
        searchInputField.sendKeys(text2);
        searchInputField.sendKeys(Keys.ENTER);
    }


    @TestStep(name = "Search Text Triple", using = "[search.text1, search.text2, search.text3]")
    public void searchTriple(String text1, String text2, String text3) {
        searchInputField.clear();
        searchInputField.sendKeys(text1);
        searchInputField.sendKeys(Keys.ENTER);
        searchInputField.clear();
        searchInputField.sendKeys(text2);
        searchInputField.sendKeys(Keys.ENTER);
        searchInputField.clear();
        searchInputField.sendKeys(text3);
        searchInputField.sendKeys(Keys.ENTER);
    }

    @TestStep(name = "Search Text Multiple", using = "searchArray")
    public void searchMultiple(String text1, String text2, String text3, String text4) {
        searchInputField.clear();
        searchInputField.sendKeys(text1);
        searchInputField.sendKeys(Keys.ENTER);
        searchInputField.clear();
        searchInputField.sendKeys(text2);
        searchInputField.sendKeys(Keys.ENTER);
        searchInputField.clear();
        searchInputField.sendKeys(text3);
        searchInputField.sendKeys(Keys.ENTER);
        searchInputField.clear();
        searchInputField.sendKeys(text4);
        searchInputField.sendKeys(Keys.ENTER);
    }

    @TestStep(name = "Search List", using = "texts.values")
    public void searchList(List<Map<String, String>> values) {
        values.forEach(text -> {
            text.forEach((key, value) -> {
                searchInputField.clear();
                logStepInfo("Search " + key + " -> " + value);
                searchInputField.sendKeys(value);
            });
        });
    }

    @TestStep(name = "Search List Line", using = "CustomizedDataMap")
    public void searchListLine(Map<String, String> line) {
        line.get("SearchText1");
    }
}
