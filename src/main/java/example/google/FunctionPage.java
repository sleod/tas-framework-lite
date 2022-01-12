package example.google;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.annotations.TestStep;
import ch.qa.testautomation.framework.core.component.TestRunManager;
import ch.qa.testautomation.framework.web.WebPageObject;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class FunctionPage extends WebPageObject {

    @FindBy(name = "q")
    protected SelenideElement searchInputField;

    @Override
    @TestStep(name = "Check Fields Present")
    public void checkFields() {
        assertTrue("Input Field is not displayed!", searchInputField.isDisplayed());
    }

    @TestStep(name = "Start Up")
    public void startUp() {
        if (PropertyResolver.getProperty("startURL") == null) {
            open("https://www.google.ch");
        } else {
            open(PropertyResolver.getProperty("startURL"));
        }
        TestRunManager.addExtraAttachment4TestCase("D:\\temp.txt");
    }

    /**
     * enter search text
     *
     * @param text for search
     */
    @TestStep(name = "Search Text", using = "searchText", takeScreenshot = true)
    public void searchText(String text) {
        searchInputField.clear();
        searchInputField.sendKeys(text);
        searchInputField.sendKeys(Keys.ENTER);
        TestRunManager.addExtraAttachment4TestCase("D:\\REST_Query.txt");

    }

    @TestStep(name = "Additional Demo Search Text", using = "additionalSearchMap")
    public void login(Map<String, Object> searchText) throws Exception {

        searchText.forEach((key, value) -> {
            searchInputField.clear();
            searchInputField.sendKeys(String.valueOf(value));
        });
    }

    @TestStep(name = "Global Search Text", using = "global.global_values")
    public void globalSearchText(Map<String, Object> searchText) throws Exception {

        searchText.forEach((key, value) -> {
            searchInputField.clear();
            searchInputField.sendKeys(String.valueOf(value));
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
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

    /**
     * Example Method for using dynamic data array as parameter
     *
     * @param listOfTexts dynamic data array with undefined length
     */
    @TestStep(name = "Search Text Array", using = "searchArray")
    public void searchTextArray(List<String> listOfTexts) {
        for (String text : listOfTexts) {
            searchInputField.clear();
            searchInputField.sendKeys(text);
            searchInputField.sendKeys(Keys.ENTER);
        }
    }

    @TestStep(name = "Search List", using = "texts")
    public void searchList(List<Map<String, String>> values) {
        values.forEach(text -> {
            text.forEach((key, value) -> {
                searchInputField.clear();
                logStepInfo("Search " + key + " -> " + value);
                searchInputField.sendKeys(value);
            });
        });
    }

    @TestStep(name = "Search List Same", using = "texts.values")
    public void searchListSame(List<Map<String, String>> values) {
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

    protected void switchToIframe(SelenideElement iframe) {
        driver.switchTo().frame(iframe);
    }

    protected void switchToMainFrame() {
        driver.switchTo().defaultContent();
    }
}
