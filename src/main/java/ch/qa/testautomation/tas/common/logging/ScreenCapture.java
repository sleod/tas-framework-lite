package ch.qa.testautomation.tas.common.logging;

import ch.qa.testautomation.tas.core.component.TestStepMonitor;
import ch.qa.testautomation.tas.core.controller.UserRobot;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import ch.qa.testautomation.tas.intefaces.ScreenshotTaker;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;

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
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
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
     * @param input image file
     * @return BufferedImage
     */
    public static BufferedImage takeScreenShot(File input) {
        try {
            return ImageIO.read(input);
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
        }

    }

    /**
     * standard method for taking screen
     *
     * @param codes base64 encoded image content
     * @return screenshot
     */
    public static Screenshot getScreenshot(String codes) {
        String testCaseName = TestStepMonitor.getCurrentTestCaseName();
        String stepName = TestStepMonitor.getCurrentTestStepName();
        Screenshot screenshot = null;
        if (isValid(codes)) {
            info("*** save screenshot to Report for Test Case: " + testCaseName + " -> " + stepName);
            byte[] decodedBytes = Base64.getMimeDecoder().decode(codes);
            try {
                screenshot = new Screenshot(ImageIO.read(new ByteArrayInputStream(decodedBytes)), testCaseName, stepName, "");
            } catch (ExceptionBase | IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
            }
        }
        return screenshot;
    }
}
