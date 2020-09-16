//package testFramework.testRemote;
//
//import ch.raiffeisen.testautomation.framework.common.logging.SystemLogger;
//import io.appium.java_client.android.AndroidDriver;
//import io.appium.java_client.android.AndroidElement;
//import io.appium.java_client.ios.IOSDriver;
//import io.appium.java_client.ios.IOSElement;
//import org.junit.Test;
//import org.openqa.selenium.Platform;
//import org.openqa.selenium.remote.DesiredCapabilities;
//import org.openqa.selenium.remote.RemoteWebDriver;
//
//import java.net.MalformedURLException;
//import java.net.URL;
//
//public class testHub {
//    @Test
//    public void iosDriverTest() throws MalformedURLException {
//        DesiredCapabilities capability = DesiredCapabilities.iphone();
//        capability.setBrowserName("safari");
//        capability.setPlatform(Platform.IOS);
//        capability.setCapability("appiumVersion", "1.7.2");
//        capability.setCapability("platformName", "iOS");
//        capability.setCapability("autoWebview", true);
//        capability.setCapability("automationName", "XCuiTest");
//        capability.setCapability("platformVersion", "10.1");
//        capability.setCapability("udid", "2a815bdbe5e805fcbf0a259f1e43c9fa10b9476b");
//        capability.setCapability("deviceName", "iPhone6s");
//        capability.setCapability("launchTimeout", 500000);
//        capability.asMap().forEach((key, value) -> SystemLogger.trace("Capability: " + key + " -> " + value));
//        IOSDriver driver = new IOSDriver<IOSElement>(new URL("http://selenium.raiffeisen.ch:4444/wd/hub"), capability);
//        driver.navigate().to("http://gmail.com");
//        driver.close();
//    }
//
//    @Test
//    public void androidDriverTest() throws MalformedURLException {
//        DesiredCapabilities capability = DesiredCapabilities.android();
//        capability.setBrowserName("Chrome");
//        capability.setPlatform(Platform.ANDROID);
//        capability.setCapability("platformName", "Android");
//        capability.setCapability("grdn_uuid", "f47116f7-a1b4-316f-94a3-4ae14bc2632e");
//        capability.setCapability("deviceName", "Galaxy_S8");
//        capability.asMap().forEach((key, value) -> SystemLogger.trace("Capability: " + key + " -> " + value));
//        AndroidDriver<?> driver = new AndroidDriver<AndroidElement>(new URL("http://192.168.206.22/selenium/wd/hub"), capability);
//        driver.navigate().to("http://gmail.com");
//        driver.close();
//    }
//
//    @Test
//    public void remoteDriverTest() throws MalformedURLException {
//        DesiredCapabilities capability = DesiredCapabilities.android();
//        capability.setBrowserName("Chrome");
//        capability.setPlatform(Platform.ANDROID);
//        capability.setCapability("platformName", "Android");
//        capability.setCapability("grdn_uuid", "f47116f7-a1b4-316f-94a3-4ae14bc2632e");
//        capability.asMap().forEach((key, value) -> SystemLogger.trace("Capability: " + key + " -> " + value));
//        RemoteWebDriver driver = new RemoteWebDriver(new URL("http://192.168.206.22/selenium/wd/hub"), capability);
//        driver.navigate().to("https://www.google.ch");
//        driver.close();
//    }
//
//}