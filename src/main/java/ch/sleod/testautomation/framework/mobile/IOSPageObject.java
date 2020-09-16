package ch.sleod.testautomation.framework.mobile;

import ch.sleod.testautomation.framework.common.abstraction.SingleTestObject;
import ch.sleod.testautomation.framework.core.component.DriverManager;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.IOSElement;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;

public abstract class IOSPageObject extends SingleTestObject {
    @SuppressWarnings("unchecked")
    protected IOSDriver<IOSElement> driver = (IOSDriver<IOSElement>) DriverManager.getAppDriver();

    public IOSPageObject() {
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    public abstract void checkFields();
}
