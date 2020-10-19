package ch.raiffeisen.testautomation.framework.web;

import ch.raiffeisen.testautomation.framework.common.abstraction.SingleTestObject;
import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import ch.raiffeisen.testautomation.framework.core.component.DriverManager;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.impl.SelenideFieldDecorator;
import com.codeborne.selenide.impl.SelenidePageFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.pagefactory.DefaultElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.DefaultFieldDecorator;

public abstract class WebPageObject extends SingleTestObject {

    protected final WebDriver driver;

    public WebPageObject() {
        if (PropertyResolver.demoModeEnabled() || PropertyResolver.isGenerateVideoEnabled()) {
            EventFiringWebDriver eventHandler = new EventFiringWebDriver(DriverManager.getWebDriver());
            eventHandler.register(new WebDriverEventCapture());
            driver = eventHandler;
        } else {
            driver = DriverManager.getWebDriver();
        }

        WebDriverRunner.setWebDriver(driver);
        SelenidePageFactory selenidePageFactory = new SelenidePageFactory();
        selenidePageFactory.initElements(new SelenideFieldDecorator(selenidePageFactory, WebDriverRunner.driver(), WebDriverRunner.getWebDriver()), this);
//        PageFactory.initElements(new DefaultFieldDecorator(new DefaultElementLocatorFactory(driver)), this);
    }

    /**
     * for initial check of fields present
     * note that, not all fields should be visible after initialization.
     * Some fields can be displayed later after some action
     */
    public abstract void checkFields();

}

