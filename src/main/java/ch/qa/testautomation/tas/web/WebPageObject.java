package ch.qa.testautomation.tas.web;

import ch.qa.testautomation.tas.common.abstraction.SingleTestObject;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.DriverManager;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.impl.SelenidePageFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

/**
 * Parent Class of all Web Page Class using PageFactory Pattern
 */
public abstract class WebPageObject extends SingleTestObject {

    protected final RemoteWebDriver driver;

    public WebPageObject() {
        RemoteWebDriver webDriver = DriverManager.getWebDriver();
        if (PropertyResolver.isDemoModeEnabled() || PropertyResolver.isGenerateVideoEnabled()) {
            driver = (RemoteWebDriver) new EventFiringDecorator<>(new WebDriverEventCapture()).decorate(webDriver);
        } else {
            driver = webDriver;
        }
        WebDriverRunner.setWebDriver(driver);
        new SelenidePageFactory().page(WebDriverRunner.driver(), this);
    }
}