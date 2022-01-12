package example.google;



import ch.qa.testautomation.framework.core.annotations.StopOnError;
import ch.qa.testautomation.framework.core.annotations.TestObject;
import ch.qa.testautomation.framework.core.annotations.TestStep;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import org.openqa.selenium.support.FindBy;

import static com.codeborne.selenide.Selenide.*;
import static org.junit.Assert.assertEquals;

@TestObject(name = "Google Home Page", definedBy = "google-definition.json")
public class HomePage extends FunctionPage {

    @FindBy()
    private SelenideElement logo;

    @FindBy()
    private SelenideElement popupIframe;

    @FindBy()
    private SelenideElement agreeButton;

    private SelenideElement hpLogo = $("#hplogo");

    @FindBy(id = "cnskx")
    private SelenideElement accept;

    @FindBy(tagName = "titl")
    private SelenideElement title;

    @TestStep(name = "Check Page Title ää", using = "pageTitle")
    @StopOnError(false)
    public void checkTitle1(String pageTitle) {
        String text = title();
        assertEquals("Page Title is wrong!", pageTitle, text);
    }

    @TestStep(name = "Check Page Title", using = "pageTitle")
    @StopOnError(false)
    public void checkTitle(String pageTitle) {
        String text = title();
        assertEquals("Page Title is wrong!", pageTitle, text);
    }

    @Override
    @TestStep(name = "Check Fields Present")
    public void checkFields() {

        if(popupIframe.exists()){
            switchToIframe(popupIframe);
            agreeButton.click();
            switchToMainFrame();
        }
        hpLogo.should(Condition.appear);
    }
}
