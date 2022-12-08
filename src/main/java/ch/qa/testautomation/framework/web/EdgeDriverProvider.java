package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import org.openqa.selenium.Point;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

import java.time.Duration;

public class EdgeDriverProvider extends WebDriverProvider {

    public void initialize() {
        EdgeOptions options = new EdgeOptions();
        if(isHeadless()){
            options.addArguments("headless", "disable-gpu");
        }
        EdgeDriver edgeDriver = new EdgeDriver(options);
        edgeDriver.manage().window().setPosition(new Point(0,0));
        edgeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        configureWindowSize(edgeDriver, isIEMaximised());
        setDriver(edgeDriver);
    }

    private boolean isIEMaximised() {
        return PropertyResolver.useMaximised();
    }

    private boolean isHeadless() {
        return PropertyResolver.useHeadlessMode();
    }
}
