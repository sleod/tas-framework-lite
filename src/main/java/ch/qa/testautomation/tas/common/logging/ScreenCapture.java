package ch.qa.testautomation.tas.common.logging;

import ch.qa.testautomation.tas.core.component.TestStepMonitor;
import ch.qa.testautomation.tas.core.controller.UserRobot;
import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;
import ch.qa.testautomation.tas.intefaces.ScreenshotTaker;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;

public class ScreenCapture {

    private static ScreenshotTaker screenShotTaker = null;

    /**
     * capture screenshot in Windows os with Robot
     *
     * @return Screenshot instance
     */
    public static Screenshot captureFullScreen() {
        String testCaseName = TestStepMonitor.getCurrentTestCaseName();
        String stepName = TestStepMonitor.getCurrentTestStepName();
        try {
            return new Screenshot(UserRobot.captureMainFullScreen(), testCaseName, stepName, "");
        } catch (AWTException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
        }
    }

    public static void setScreenTaker(ScreenshotTaker taker) {
        screenShotTaker = taker;
    }

    /**
     * command to take screenshot
     *
     * @return Screenshot instance
     */
    public static Screenshot takeScreenShot() {
        if (screenShotTaker != null) {
            return screenShotTaker.takeScreenShot();
        } else {
            return captureFullScreen();
        }
    }

    /**
     * take screenshot manuel
     *
     * @param shooter screenshots taker
     * @return BufferedImage
     */
    public static BufferedImage takeScreenShot(TakesScreenshot shooter) {
        try {
            return ImageIO.read(shooter.getScreenshotAs(OutputType.FILE));
        } catch (IOException ex) {
            throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
        }

    }

    /**
     * standard method for taking screen
     *
     * @param shooter TakesScreenshot
     * @return screenshot
     */
    public static Screenshot getScreenshot(TakesScreenshot shooter) {
        String testCaseName = TestStepMonitor.getCurrentTestCaseName();
        String stepName = TestStepMonitor.getCurrentTestStepName();
        Screenshot screenshot = null;
        if (shooter != null) {
            info("*** save screenshot to Report for Test Case: " + testCaseName + " -> " + stepName);
            String imageData = shooter.getScreenshotAs(OutputType.BASE64);
            byte[] decodedBytes = Base64.getMimeDecoder().decode(imageData);
            try {
                screenshot = new Screenshot(ImageIO.read(new ByteArrayInputStream(decodedBytes)), testCaseName, stepName, "");
            } catch (ApollonBaseException | IOException ex) {
                throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
            }
        }
        return screenshot;
    }
}
