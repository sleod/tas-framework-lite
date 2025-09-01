package io.github.sleod.tas.web;

import io.github.sleod.tas.common.utils.WaitUtils;
import io.github.sleod.tas.configuration.PropertyResolver;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

public class WebDriverEventCapture implements WebDriverListener {

    /**
     * perform action after find any element
     *
     * @param driver  web driver
     * @param by      selector
     * @param element element
     */
    @Override
    public void afterFindElement(WebDriver driver, By by, WebElement element) {
        if (PropertyResolver.isDemoModeEnabled()) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            String style = "";
            if (element.getDomAttribute("style") != null) {
                style = element.getDomAttribute("style");
            }
            js.executeScript("arguments[0].style.border='2px solid red'", element);
            WaitUtils.waitStepMilli(200);
            js.executeScript("arguments[0].setAttribute('style', arguments[1])", element, style);
        }
    }
}
