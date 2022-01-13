package ch.qa.testautomation.framework.mobile;

import ch.qa.testautomation.framework.core.component.DriverManager;
import io.appium.java_client.android.Activity;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import org.openqa.selenium.support.PageFactory;

public abstract class AndroidPageObject extends MobileTestObject {

    protected final AndroidDriver driver = (AndroidDriver) DriverManager.getAppDriver();

    public void initialize() {
        PageFactory.initElements(new AppiumFieldDecorator(driver), this);
    }

    public abstract void checkFields();

    public void startActvity(String appName, String activity) {
        setAppName(appName);
        driver.startActivity(new Activity(appName, activity));
    }
}
