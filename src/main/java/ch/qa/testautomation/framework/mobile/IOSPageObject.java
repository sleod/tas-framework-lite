package ch.qa.testautomation.framework.mobile;

import ch.qa.testautomation.framework.core.component.DriverManager;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;

public abstract class IOSPageObject extends MobileTestObject {
    protected IOSDriver driver = (IOSDriver) DriverManager.getAppDriver();

    public IOSPageObject() {
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    public abstract void checkFields();
}
