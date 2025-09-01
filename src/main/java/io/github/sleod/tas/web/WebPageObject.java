package io.github.sleod.tas.web;

import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.impl.SelenidePageFactory;
import io.github.sleod.tas.common.abstraction.SingleTestObject;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.core.component.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.events.EventFiringDecorator;

/**
 * Parent Class of all Web Page Class using PageFactory Pattern
 */
public abstract class WebPageObject extends SingleTestObject {

    protected final WebDriver driver;

    public WebPageObject() {
        WebDriver webDriver = DriverManager.getWebDriver();
        if (PropertyResolver.isDemoModeEnabled() || PropertyResolver.isGenerateVideoEnabled()) {
            driver = new EventFiringDecorator<>(new WebDriverEventCapture()).decorate(webDriver);
        } else {
            driver = webDriver;
        }
        WebDriverRunner.setWebDriver(driver);
        new SelenidePageFactory().page(WebDriverRunner.driver(), this);
    }
}