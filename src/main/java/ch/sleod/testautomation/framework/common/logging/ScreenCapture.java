package ch.sleod.testautomation.framework.common.logging;

import ch.sleod.testautomation.framework.core.component.TestStepMonitor;
import ch.sleod.testautomation.framework.core.controller.UserRobot;
import ch.sleod.testautomation.framework.intefaces.ScreenshotTaker;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.awt.*;
import java.awt.image.BufferedImage;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;

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
        } catch (AWTException ex) {
            error(ex);
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
    public static BufferedImage takeScreenShot(TakesScreenshot shooter) {
        byte[] imageData = shooter.getScreenshotAs(OutputType.BYTES);
        return Screenshot.createImageFromBytes(imageData);
    }
}
