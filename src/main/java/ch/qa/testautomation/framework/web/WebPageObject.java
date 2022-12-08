package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.common.abstraction.SingleTestObject;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.DriverManager;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.impl.SelenidePageFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

/**
 * Parent Class of all Web Page Class using PageFactory Pattern
 */
public abstract class WebPageObject extends SingleTestObject {

    protected final WebDriver driver;

    public WebPageObject() {
        WebDriver tempDriver = DriverManager.getWebDriver();
        if (PropertyResolver.demoModeEnabled() || PropertyResolver.isGenerateVideoEnabled()) {
            driver = new EventFiringDecorator<>(new WebDriverEventCapture()).decorate(tempDriver);
        } else {
            driver = tempDriver;
        }
        WebDriverRunner.setWebDriver(driver);
        new SelenidePageFactory().page(WebDriverRunner.driver(), this);
    }
}