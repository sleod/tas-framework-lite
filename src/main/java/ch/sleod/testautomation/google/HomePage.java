package ch.sleod.testautomation.google;

import ch.sleod.testautomation.framework.common.utils.WebOperationUtils;
import ch.sleod.testautomation.framework.core.annotations.StopOnError;
import ch.sleod.testautomation.framework.core.annotations.TestObject;
import ch.sleod.testautomation.framework.core.annotations.TestStep;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.Annotations;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestObject(name = "Google Home Page", definedBy = "google-definition.json")
public class HomePage extends FunctionPage {

    @FindBy(id = "hplog")
    private WebElement logo;

    @FindBy(id = "cnskx")
    private WebElement accept;

    @FindBy(tagName = "titl")
    private WebElement title;

    @TestStep(name = "Check Page Title", using = "pageTitle")
    @StopOnError(false)
    public void checkTitle(String pageTitle) {
        String text = driver.getTitle();
//        Assertion.assessTrue("Page Title is wrong! Actual: " + text + " but expected: " + pageTitle, text.equalsIgnoreCase(pageTitle));
        assertEquals("Page Title is wrong!", text, pageTitle);
    }

    @Override
    @TestStep(name = "Check Fields Present")
    public void checkFields() {
//        WebOperationUtils.waitUntilVisible(driver, logo);
        try {
            Annotations annotations = new Annotations(getClass().getDeclaredField("accept"));
            By by = annotations.buildBy();
            System.out.println(by);
            WebOperationUtils.waitForElementLoad(driver, by).click();

            annotations = new Annotations(getClass().getDeclaredField("logo"));
            by = annotations.buildBy();
            System.out.println(by);
            WebOperationUtils.waitForElementLoad(driver, by);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }


//        assertTrue("Logo is not displayed!", logo.isEnabled());
//        assertTrue("Input Field is not displayed!", searchInputField.isEnabled());
    }

}
