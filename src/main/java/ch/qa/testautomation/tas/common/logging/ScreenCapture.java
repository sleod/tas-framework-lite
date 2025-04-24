package ch.qa.testautomation.tas.common.logging;

import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.component.TestStepMonitor;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import ch.qa.testautomation.tas.intefaces.DriverProvider;
import ch.qa.testautomation.tas.web.ChromeDriverProvider;
import ch.qa.testautomation.tas.web.PlaywrightDriverProvider;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v131.emulation.Emulation;
import org.openqa.selenium.devtools.v85.page.Page;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.warn;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;
import static com.codeborne.selenide.Selenide.executeJavaScript;

public class ScreenCapture {

    private static DriverProvider screenShotTaker = null;

    public static void setScreenTaker(DriverProvider taker) {
        screenShotTaker = taker;
    }

    /**
     * command to take screenshot
     *
     * @return Screenshot instance
     */
    public static Screenshot takeScreenShot() {
        if (PropertyResolver.isCDPEnabled()) {
            return takeFullScreen();
        } else {
            return takeScreen();
        }
    }

    /**
     * Take Screenshot of Step
     *
     * @return Screenshot
     */

    public static Screenshot takeScreenShot(String name, String location) {
        return getScreenshot(getCamara().getScreenshotAs(OutputType.BASE64), name, location);
    }

    /**
     * standard method for taking screen
     *
     * @param codes    base64 encoded image content
     * @param name     base64 encoded image content
     * @param location customized location to save
     * @return screenshot
     */
    public static Screenshot getScreenshot(String codes, String name, String location) {
        String testCaseName = TestStepMonitor.getCurrentTestCaseName();
        String stepName = TestStepMonitor.getCurrentTestStepName();
        Screenshot screenshot = null;
        if (isValid(codes)) {
            info("*** save screenshot in: " + location);
            byte[] decodedBytes = Base64.getMimeDecoder().decode(codes);
            try {
                screenshot = new Screenshot(ImageIO.read(new ByteArrayInputStream(decodedBytes)), testCaseName, stepName, name, location);
                info("with name: " + screenshot.getScreenshotFile().getPath());
            } catch (ExceptionBase | IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
            }
        }
        return screenshot;
    }

    public static DriverProvider getScreenShotTaker() {
        if (screenShotTaker == null) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "No Screenshot taker available!");
        }
        return screenShotTaker;
    }

    private static Screenshot getScreenshot(File screenshotFile) {
        Screenshot screenshot = null;
        if (screenshotFile != null && screenshotFile.exists()) {
            screenshot = createScreenshot(screenshotFile);
        } else {
            warn("Fail on taking Screenshot: String codes are not valid!");
        }
        return screenshot;
    }

    /**
     * standard method for taking screen
     *
     * @param codes base64 encoded image content
     * @return screenshot
     */
    private static Screenshot getScreenshot(String codes) {
        Screenshot screenshot = null;
        if (isValid(codes)) {
            screenshot = createScreenshot(codes);
        } else {
            warn("Fail on taking Screenshot: String codes are not valid!");
        }
        return screenshot;
    }

    private static Screenshot createScreenshot(String codes) {
        String testCaseName = TestStepMonitor.getCurrentTestCaseName();
        String stepName = TestStepMonitor.getCurrentTestStepName();
        info("*** save screenshot: " + testCaseName + "/" + stepName);
        try {
            byte[] decodedBytes = Base64.getMimeDecoder().decode(codes);
            Screenshot screenshot = new Screenshot(ImageIO.read(new ByteArrayInputStream(decodedBytes)), testCaseName, stepName);
            info("with name: " + screenshot.getScreenshotFile().getPath());
            return screenshot;
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
        }
    }

    private static Screenshot createScreenshot(File screenshotFile) {
        String testCaseName = TestStepMonitor.getCurrentTestCaseName();
        String stepName = TestStepMonitor.getCurrentTestStepName();
        info("*** save screenshot: " + testCaseName + "/" + stepName);
        try {
            Screenshot screenshot = new Screenshot(ImageIO.read(screenshotFile), testCaseName, stepName);
            info("with name: " + screenshot.getScreenshotFile().getPath());
            return screenshot;
        } catch (IOException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_SCREEN_CAPTURE, ex);
        }
    }

    private static Screenshot takeScreen() {
        if (getScreenShotTaker() instanceof PlaywrightDriverProvider playwrightDriverProvider) {
            return getScreenshot(playwrightDriverProvider.getDriver().screenshot(false));
        } else {
            return getScreenshot(getCamara().getScreenshotAs(OutputType.BASE64));
        }
    }

    private static Screenshot takeFullScreen() {
        if (getScreenShotTaker() instanceof PlaywrightDriverProvider playwrightDriverProvider) {
            File filePath = playwrightDriverProvider.getDriver().screenshot(true);
            return getScreenshot(filePath);
        } else {
            Number width = executeJavaScript("return document.body.scrollWidth");
            Number height = executeJavaScript("return document.body.scrollHeight");
            Number scale = executeJavaScript("return window.devicePixelRatio");
            DevTools devTools = getDevTools();
            // Override device metrics to simulate full page
            devTools.send(Emulation.setDeviceMetricsOverride(
                    width.intValue(),                                         // Width
                    height.intValue(),                                        // Height (full page height)
                    scale,                                                    // Device scale factor
                    false,  // Mobile device (false for desktop)
                    Optional.of(scale),                                       // Scale
                    Optional.of(width.intValue()),  // Screen width
                    Optional.of(height.intValue()), // Screen height
                    Optional.empty(),               // Position X
                    Optional.empty(),               // Position Y
                    Optional.of(false),       // Don't set visible size
                    Optional.empty(),               // Screen orientation
                    Optional.empty(),               // Viewport
                    Optional.empty(),               // Display feature
                    Optional.empty()                // devicePosture
            ));
            // Send command to capture full page screenshot
            return getScreenshot(devTools.send(Page.captureScreenshot(
                    Optional.of(Page.CaptureScreenshotFormat.PNG), // Output format
                    Optional.empty(),            // Quality (only applies to JPEG)
                    Optional.empty(),            // Clip
                    Optional.empty()             // From surface
            )));
        }
    }

    private static DevTools getDevTools() {
        DevTools devTools;
        if (getScreenShotTaker() instanceof ChromeDriverProvider chromeDriverProvider) {
            devTools = chromeDriverProvider.getDevTools();
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Actual WebDriver do not have CDP Usage!");
        }
        devTools.createSession();
        return devTools;
    }

    private static TakesScreenshot getCamara() {
        return getScreenShotTaker().getDriver();
    }

}
