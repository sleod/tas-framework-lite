package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.common.abstraction.SingleTestObject;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.DriverManager;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.impl.SelenidePageFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.openqa.selenium.support.events.WebDriverListener;

public abstract class WebPageObject extends SingleTestObject {

    protected final WebDriver driver;

    public WebPageObject() {
        WebDriver tempDriver = DriverManager.getWebDriver();
        if (PropertyResolver.demoModeEnabled() || PropertyResolver.isGenerateVideoEnabled()) {
            WebDriverListener listener = new WebDriverEventCapture();
            driver = new EventFiringDecorator(listener).decorate(tempDriver);
        } else {
            driver = tempDriver;
        }
        WebDriverRunner.setWebDriver(driver);
        SelenidePageFactory selenidePageFactory = new SelenidePageFactory();
        selenidePageFactory.initElements(WebDriverRunner.driver(), null, this, null);
//        selenidePageFactory.initElements(new SelenideFieldDecorator(selenidePageFactory, WebDriverRunner.driver(), WebDriverRunner.getWebDriver()), this);
//        PageFactory.initElements(new DefaultFieldDecorator(new DefaultElementLocatorFactory(driver)), this);
    }

    /**
     * for initial check of fields present
     * note that, not all fields should be visible after initialization.
     * Some fields can be displayed later after some action
     */
    @Deprecated
    public void checkFields() {
    }

}
