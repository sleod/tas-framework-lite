package ch.sleod.testautomation.framework.web;

import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

import java.util.concurrent.TimeUnit;

public class IEDriverProvider extends WebDriverProvider {

    public void initialize() {
        InternetExplorerOptions options = new InternetExplorerOptions()
                .ignoreZoomSettings()
                .introduceFlakinessByIgnoringSecurityDomains()
                .requireWindowFocus();
        if(isKeepIECache()) {
            options.destructivelyEnsureCleanSession();
        }
        //focued create process
        InternetExplorerDriver ieDriver = new InternetExplorerDriver(options);
        String[] point = System.getProperty("WebDriverPosition", "400,0").split(",");
        String[] size = System.getProperty("WebDriverSize", "1500,1000").split(",");
        Point po = new Point(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
        Dimension di = new Dimension(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
        ieDriver.manage().window().setPosition(po);
        ieDriver.manage().window().setSize(di);
        ieDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        if (isIEMaximised()){
            ieDriver.manage().window().maximize();
        }
        setDriver(ieDriver);
    }

    private boolean isIEMaximised() {
        return PropertyResolver.useMaximised();
    }
    private boolean isKeepIECache() {
        return PropertyResolver.keepIECache();
    }
}
