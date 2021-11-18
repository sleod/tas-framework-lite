package ch.qa.testautomation.framework.core.controller;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.utils.ZipUtils;
import ch.qa.testautomation.framework.core.json.container.JSONRunnerConfig;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.rest.TFS.connection.TFSRestClient;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExternAppController {

    private static final String matchPattern = "(\\d+)[\\.](\\d+)[\\.](\\d+)[\\.](\\d+)";
    private static final Pattern pattern = Pattern.compile(matchPattern);
    private static final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * use external app to edit text file
     *
     * @param file text based file
     */
    public static void viewTextWithNotepad(File file) {
        String textEditor = PropertyResolver.getTextEditor();
        String command = "startNow " + textEditor + " \"" + file.getAbsolutePath();
        executeCommand(command);
    }

    /**
     * set ocr language: deu, eng
     *
     * @param language such as: deu, eng
     */
    public static void setLanguage(String language) {
        OCRController.setLanguage(language);
    }

    /**
     * set Tess Page IteratorLevel
     *
     * @param level such as: ITessAPI.TessPageIteratorLevel.RIL_WORD
     */
    public static void setIteratorLevel(int level) {
        OCRController.setIteratorLevel(level);
    }

    /**
     * find element with its label text and click on
     *
     * @param text    label text of element
     * @param waitSec wait for sec after click if value big than 0
     * @throws AWTException AWT Exception
     */
    public static Rectangle findElementAndLeftClick(String text, boolean doubleClick, int waitSec) throws AWTException {
        BufferedImage bufferedImage = UserRobot.captureMainFullScreen();
        Rectangle element = OCRController.findTextPosition(text, bufferedImage);
        leftClickOnElement(element, doubleClick, waitSec);
        return element;
    }

    public static boolean checkElementPresentsWithText(String text) throws AWTException {
        BufferedImage bufferedImage = UserRobot.captureMainFullScreen();
        return OCRController.checkTextLineWithText(text, bufferedImage);
    }

    /**
     * find element with its label text and click on
     *
     * @param text    label text of element
     * @param waitSec wait for sec after click if value big than 0
     * @throws AWTException AWT Exception
     */
    public static Rectangle findElementAndRightClick(String text, boolean doubleClick, int waitSec) throws AWTException {
        BufferedImage bufferedImage = UserRobot.captureMainFullScreen();
        Rectangle element = OCRController.findTextPosition(text, bufferedImage);
        rightClickOnElement(element, doubleClick, waitSec);
        return element;
    }

    /**
     * find element with its label text and click on
     *
     * @param element element to click
     * @param waitSec wait for sec after click if value big than 0
     * @throws AWTException AWT Exception
     */
    public static void leftClickOnElement(Rectangle element, boolean doubleClick, int waitSec) throws AWTException {
        UserRobot.leftClickOn(element, doubleClick);
        if (waitSec > 0) {
            sleep(waitSec);
        }
    }

    /**
     * send text to current element
     *
     * @param text text to send
     * @throws AWTException AWT Exceptions
     */
    public static void sendText(String text) throws AWTException {
        UserRobot.sendText(text);
    }

    /**
     * Press keys
     *
     * @param keys keys array {@link java.awt.event.KeyEvent}
     * @throws AWTException awt exception
     */
    public static void pressKeys(int... keys) throws AWTException {
        if (keys.length == 1) {
            UserRobot.pressKey(keys[0]);
        } else if (keys.length == 2) {
            UserRobot.pressKeys(keys[0], keys[1]);
        } else if (keys.length == 3) {
            UserRobot.pressKeys(keys[0], keys[1], keys[2]);
        } else if (keys.length != 0) {
            throw new RuntimeException("Key Combination more than 3 keys is provided yet!");
        }
        sleep(1);
    }

    /**
     * find element with its label text and click on
     *
     * @param element element to click
     * @param waitSec wait for sec after click if value big than 0
     * @throws AWTException AWT Exception
     */
    public static void rightClickOnElement(Rectangle element, boolean doubleClick, int waitSec) throws AWTException {
        UserRobot.rightClickOn(element, doubleClick);
        if (waitSec > 0) {
            sleep(waitSec);
        }
    }

    /**
     * Quick execute command regardless process state
     *
     * @param command command
     */
    public static void runCommandAndWait(String command, int sec) {
        try {
            Runtime.getRuntime().exec(command);
            if (sec > 0) {
                sleep(sec);
            }
        } catch (IOException ex) {
            SystemLogger.error(ex);
        }

    }


    /**
     * execute command
     *
     * @param command command to execute
     * @return output of process
     */
    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process;
            if (PropertyResolver.isWindows()) {
                command = String.format("cmd.exe /c %s", command);
                process = Runtime.getRuntime().exec(command);
            } else {
                command = String.format("sh -c %s", command);
                process = Runtime.getRuntime().exec(command);
            }
            SystemLogger.trace("Executing command: " + command);
            int exitCode = process.waitFor();
            SystemLogger.trace("Process exit code: " + exitCode);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            SystemLogger.trace("Output: " + output.toString());
        } catch (IOException | InterruptedException ex) {
            SystemLogger.error(ex);
        }
        return output.toString();
    }

    public static String executeCommandInDir(String targetDir, String command) {
        StringBuilder output = new StringBuilder();
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("cmd.exe", "/c", command);
        processBuilder.directory(new File(targetDir));
        SystemLogger.trace("Executing command: " + command);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            int exitCode = process.waitFor();
            SystemLogger.trace("Output: " + output.toString());
        } catch (IOException | InterruptedException ex) {
            SystemLogger.error(ex);
        }
        return output.toString();
    }

    /**
     * check current chrome and driver version
     *
     * @return matched driver path or exception
     */
    public static Path matchChromeAndDriverVersion() throws IOException {
        String chromeVersion = getCurrentChromeVersion();
        //scan driver in folder
        String folderPath = PropertyResolver.getDefaultWebDriverBinLocation();
        String driverDir = FileLocator.getProjectBaseDir() + "/src/main/resources/" + folderPath;
        new File(driverDir).mkdirs();
        List<Path> paths = FileLocator.listRegularFilesRecursiveMatchedToName(driverDir, 3, "chromedriver");
        Path driverPath = null;
        if (!paths.isEmpty()) {
            driverPath = checkPaths(paths, chromeVersion);
        }
        if (paths.isEmpty() || driverPath == null) {
            SystemLogger.warn("No File matched to current chrome browser! Try to download from TFS source!");
            String folder = downloadChromeDrivers();
            paths = FileLocator.listRegularFilesRecursiveMatchedToName(folder, 3, "chromedriver");
            driverPath = checkPaths(paths, chromeVersion);
        }
        if (driverPath != null) {
            SystemLogger.trace("Find Driver: " + driverPath.toFile().getName() + " with Version: " + chromeVersion);
            return driverPath;
        } else {
            throw new RuntimeException("No matched driver for current test browser was found.");
        }
    }

    private static Path checkPaths(List<Path> paths, String chromeVersion) {
        Path driverPath = null;
        for (Path path : paths) {
            //check driver version with filepath --v
            String response = executeCommand(path.toString() + " --v");
            Matcher matcher = pattern.matcher(response);
            if (matcher.find()) {
                String driverVersion = matcher.group(1);
                //match chrome version with existing driver and return driver path
                if (driverVersion.equalsIgnoreCase(chromeVersion)) {
                    driverPath = path;
                    if (path.toFile().getName().toLowerCase().endsWith("exe") == PropertyResolver.isWindows()) {
                        break;
                    }
                }
            } else {
                SystemLogger.warn("No Version was found with given Pattern like 'xx.xxx.xxx.xxx' -> " + response);
                SystemLogger.warn("The Chrome Driver may too old! Please Delete it.");
            }
        }
        return driverPath;
    }

    /**
     * get current chrome version
     *
     * @return version in string
     */
    public static String getCurrentChromeVersion() {
        String response;
        if (PropertyResolver.isWindows()) {
            response = executeCommand("reg query \"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\" /v version");
        } else if (PropertyResolver.isLinux()) {
            response = executeCommand("google-chrome --version");
        } else if (PropertyResolver.isMac()) {
            response = executeCommand("/Applications/Google\\ Chrome.app/Contents/MacOS/Google\\ Chrome --version");
        } else {
            throw new RuntimeException("Unknown Operation System!");
        }
        Matcher matcher = pattern.matcher(response);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            SystemLogger.warn("No Version was found with given Pattern -> " + response);
            return response;
        }
    }

    /**
     * download chrome drivers fome TFS resources
     *
     * @return folder of drivers
     * @throws IOException io exception
     */
    public static String downloadChromeDrivers() throws IOException {
        JSONRunnerConfig runnerConfig = JSONContainerFactory.loadRunnerConfig(PropertyResolver.getTFSRunnerConfigFile());
        Map<String, String> config = runnerConfig.getTfsConfig();
        TFSRestClient tfsRestClient = new TFSRestClient(config.get("host"), config.get("pat"), config.get("organization"),
                config.get("collection"), "ap.testtools", config.get("apiVersion"));
        String filePath = FileLocator.getProjectBaseDir() + "/src/main/resources/" + PropertyResolver.getDefaultWebDriverBinLocation() + "/drivers.zip";
        File target = new File(filePath);
        SystemLogger.warn("Write to target: " + target.getAbsolutePath());
        tfsRestClient.downloadFilesAsZip(PropertyResolver.getRemoteWebDriverFolder(), target);
        ZipUtils.unzipFileHere(target);
        String path = target.getParentFile().getAbsolutePath();
        target.deleteOnExit();
        return path;
    }

    public static void sleep(int sec) {
        try {
            TimeUnit.SECONDS.sleep(sec);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static void setPageSegMode(int mode) {
        OCRController.setPageSegMode(mode);
    }

    public static void setOcrEngineMode(int mode) {
        OCRController.setOcrEngineMode(mode);
    }

    /**
     * move mouse to coordination
     *
     * @param x x coords
     * @param y y coords
     * @throws AWTException awt exception
     */
    public static void moveMouseTo(int x, int y) throws AWTException {
        UserRobot.moveMouseTo(x, y);
    }

    /**
     * find sub image in a big image with both specific size
     *
     * @param subImage                    sub image
     * @param subWidth                    width of sub image
     * @param subHeight                   high of sub image
     * @param allowedPixelFailsPercent    percentage of allowed pixel fails
     * @param allowedPixelColorDifference allowed pixel color diff
     * @return Rectangle of sub image, null if not found
     */
    public static Rectangle findSubImageOnScreen(BufferedImage subImage, int subWidth, int subHeight, double allowedPixelFailsPercent, int allowedPixelColorDifference) throws AWTException {
        return FindImageInImage.findSubImage(subImage, subWidth, subHeight, UserRobot.captureMainFullScreen(), screenSize.width, screenSize.height, allowedPixelFailsPercent, allowedPixelColorDifference);
    }
} 
