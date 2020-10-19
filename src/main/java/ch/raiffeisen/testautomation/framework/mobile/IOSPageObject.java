package ch.raiffeisen.testautomation.framework.mobile;

import ch.raiffeisen.testautomation.framework.common.abstraction.SingleTestObject;
import ch.raiffeisen.testautomation.framework.core.component.DriverManager;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;

public abstract class IOSPageObject extends MobileTestObject {
    @SuppressWarnings("unchecked")
    protected IOSDriver<IOSElement> driver = (IOSDriver<IOSElement>) DriverManager.getAppDriver();

    public IOSPageObject() {
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    public abstract void checkFields();
}
