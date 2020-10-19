package ch.raiffeisen.testautomation.google;

import ch.raiffeisen.testautomation.framework.common.utils.WebOperationUtils;
import ch.raiffeisen.testautomation.framework.core.annotations.StopOnError;
import ch.raiffeisen.testautomation.framework.core.annotations.TestObject;
import ch.raiffeisen.testautomation.framework.core.annotations.TestStep;
import ch.raiffeisen.testautomation.framework.core.assertion.Assertion;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import net.sourceforge.lept4j.Sel;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;

import static com.codeborne.selenide.Selenide.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestObject(name = "Google Home Page", definedBy = "google-definition.json")
public class HomePage extends FunctionPage {

    @FindBy(id = "hplog")
    private WebElement logo;

    private SelenideElement hpLogo = $("#hplogo");

    @FindBy(id = "cnskx")
    private SelenideElement accept;

    @FindBy(tagName = "titl")
    private WebElement title;

    @TestStep(name = "Check Page Title", using = "pageTitle")
    @StopOnError(false)
    public void checkTitle(String pageTitle) {
        String text = title();
//        String text = driver.getTitle();
//        Assertion.assessTrue("Page Title is wrong! Actual: " + text + " but expected: " + pageTitle, text.equalsIgnoreCase(pageTitle));
        assertEquals("Page Title is wrong!", pageTitle, text);
    }

    @Override
    @TestStep(name = "Check Fields Present")
    public void checkFields() {
        WebOperationUtils.waitUntilVisible(driver, logo);
        hpLogo.should(Condition.appears);
        assertTrue("Logo is not displayed!", hpLogo.exists());
        assertTrue("Logo is not displayed!", logo.isDisplayed());
        new Actions(driver).moveToElement(logo).click(logo).build().perform();
    }

}
