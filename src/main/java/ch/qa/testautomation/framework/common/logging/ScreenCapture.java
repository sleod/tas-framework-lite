package ch.qa.testautomation.framework.common.logging;

import ch.qa.testautomation.framework.core.component.TestStepMonitor;
import ch.qa.testautomation.framework.core.controller.UserRobot;
import ch.qa.testautomation.framework.intefaces.ScreenshotTaker;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.error;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.info;

public class ScreenCapture {

    private static ScreenshotTaker screenShotTaker = null;

    /**
     * capture screen shot in windows os with Robot
     *
     * @param testStepMonitor step monitor
     * @return Screenshot instance
     */
    public static Screenshot captureFullScreen(TestStepMonitor testStepMonitor) {
        String testCaseName = testStepMonitor.getCurrentTestCase().getDisplayName();
        String stepName = testStepMonitor.getLastStep().getMethodName();
        Screenshot screenshot = null;
        try {
            screenshot = new Screenshot(UserRobot.captureMainFullScreen(), testCaseName, stepName);
        } catch (AWTException | IOException ex) {
            error(new RuntimeException("Exception while capture screen!"));
        }
        return screenshot;
    }

    public static void setScreenTaker(ScreenshotTaker taker) {
        screenShotTaker = taker;
    }

    /**
     * command to take screenshot
     *
     * @param testStepMonitor test step monitor for test
     * @return Screenshot instance
     */
    public static Screenshot takeScreenShot(TestStepMonitor testStepMonitor) {
        if (screenShotTaker != null) {
            return screenShotTaker.takeScreenShot(testStepMonitor);
        } else {
            return captureFullScreen(testStepMonitor);
        }
    }

    /**
     * take screenshot manuel
     *
     * @param shooter screenshots taker
     * @return BufferedImage
     */
    public static BufferedImage takeScreenShot(TakesScreenshot shooter) throws IOException {
        byte[] imageData = shooter.getScreenshotAs(OutputType.BYTES);
        return Screenshot.createImageFromBytes(imageData);
    }

    /**
     * standard method for taking screen
     *
     * @param testStepMonitor test step monitor
     * @param driver          RemoteWebDriver
     * @return screenshot
     */
    public static Screenshot getScreenshot(TestStepMonitor testStepMonitor, RemoteWebDriver driver) {
        String testCaseName = testStepMonitor.getCurrentTestCase().getDisplayName();
        String stepName = testStepMonitor.getLastStep().getMethodName();
        Screenshot screenshot = null;
        if (driver != null) {
            info("*** save screenshot to Report for Test Case: " + testCaseName + " -> " + stepName);
            byte[] imageData = driver.getScreenshotAs(OutputType.BYTES);
            try {
//                screenshot = new Screenshot(imageData, testCaseName, stepName, driver.getPageSource());
                screenshot = new Screenshot(imageData, testCaseName, stepName, "");
            } catch (RuntimeException | IOException ex) {
                error(ex);
            }
        }
        return screenshot;
    }
}
