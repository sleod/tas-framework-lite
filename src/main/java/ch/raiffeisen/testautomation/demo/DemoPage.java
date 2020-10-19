package ch.raiffeisen.testautomation.demo;

import ch.raiffeisen.testautomation.framework.core.annotations.TestObject;
import ch.raiffeisen.testautomation.framework.core.annotations.TestStep;
import ch.raiffeisen.testautomation.framework.web.WebPageObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

@TestObject(name = "Demo Page")
public class DemoPage extends WebPageObject {

    @FindBy(id = "demo_basic")
    private WebElement list;


    @Override
    @TestStep(name = "Test Demo")
    public void checkFields() {
        Select selector = new Select(list);
//        selector.deselectByValue("2");
        selector.selectByVisibleText("Two");
    }
}
