package ch.qa.testautomation.framework.mobile;

import ch.qa.testautomation.framework.core.component.DriverManager;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;

public abstract class AndroidPageObject extends MobileTestObject {

    @SuppressWarnings("unchecked")
    protected final AndroidDriver<AndroidElement> driver = (AndroidDriver<AndroidElement>) DriverManager.getAppDriver();

    public void initialize() {
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    public abstract void checkFields();

    public void startActvity(String appName, String activity) {
        setAppName(appName);
        driver.startActivity(new Activity(appName, activity));
    }
}
