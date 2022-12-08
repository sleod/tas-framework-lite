package ch.qa.testautomation.framework.core.controller;

import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.utils.ZipUtils;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.container.JSONRunnerConfig;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
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

import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.warn;
import static ch.qa.testautomation.framework.exception.ApollonErrorKeys.CUSTOM_MESSAGE;
import static ch.qa.testautomation.framework.exception.ApollonErrorKeys.DOWNLOAD_WEB_DRIVER_NOT_DEVELOPED_YET;

public class ExternAppController {

    private static final String matchPattern = "(\\d+)[.](\\d+)[.](\\d+)[.](\\d+)";
    private static final Pattern pattern = Pattern.compile(matchPattern);

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
     * @param waitSec wait for sec after click if value bigger than 0
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
     * @param waitSec wait for sec after click if value bigger than 0
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
     * @param waitSec wait for sec after click if value bigger than 0
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
        } else {
            throw new ApollonBaseException(CUSTOM_MESSAGE, "Empty Keys or Key Combination more than 3 keys is not provided yet!");
        }
        sleep(1);
    }

    /**
     * find element with its label text and click on
     *
     * @param element element to click
     * @param waitSec wait for sec after click if value bigger than 0
     * @throws AWTException AWT Exception
     */
    public static void rightClickOnElement(Rectangle element, boolean doubleClick, int waitSec) throws AWTException {
        UserRobot.rightClickOn(element, doubleClick);
        if (waitSec > 0) {
            sleep(waitSec);
        }
    }


    /**
     * execute command
     *
     * @param command command to execute
     * @return output of process
     */
    public static String[] executeCommand(String command) {
        ProcessBuilder builder = new ProcessBuilder();
        StringBuilder output = new StringBuilder();
        int exitCode = 0;
        try {
            if (PropertyResolver.isWindows()) {
                builder.command("cmd.exe", "/c", command);
            } else {
                builder.command("sh", "-c", command);
            }
            trace("Executing command: " + command);
            Process process = builder.start();
            exitCode = process.waitFor();
            trace("Process exit code: " + exitCode);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            trace("Output: " + output);
        } catch (IOException | InterruptedException ex) {
            throw new ApollonBaseException(CUSTOM_MESSAGE, "Exception while execute command line: " + command, ex);
        }
        return new String[]{String.valueOf(exitCode), output.toString()};
    }

    /**
     * prepare chrome driver
     *
     * @return Path of driver file
     */
    public static Path prepareChromeDriver() {
        return matchBrowserAndDriverVersion(getCurrentChromeVersion(), PropertyResolver.getChromeDriverFileName());
    }

    /**
     * prepare edge driver
     *
     * @return Path of driver file
     */
    public static Path prepareEdgeDriver() {
        return matchBrowserAndDriverVersion(getCurrentEdgeVersion(), PropertyResolver.getEdgeDriverFileName());
    }

    /**
     * check current browser and driver version
     *
     * @return matched driver path or exception
     */
    private static Path matchBrowserAndDriverVersion(String browserVersion, String driverFileName) {
        File driverFile = new File(PropertyResolver.getWebDriverBinLocation());//direct input absolut filepath
        if (driverFile.exists() && driverFile.isFile()) {
            return driverFile.toPath();
        } else {
            //scan driver in folder
            String driverDir = FileLocator.getProjectBaseDir() + "/src/main/resources/" + PropertyResolver.getWebDriverBinLocation();
            new File(driverDir).mkdirs();
            List<Path> paths = FileLocator.listRegularFilesRecursiveMatchedToName(driverDir, 3, driverFileName);
            Path driverPath = null;
            if (!paths.isEmpty()) {//in case found existing driver files, check version
                driverPath = checkDriverVersionWithBrowser(paths, browserVersion);
            }
            if (paths.isEmpty() || driverPath == null) {//in case existing not match or nothing found, try to download
                warn("No File matched to current browser! Try to download driver!");
                boolean success = tryToDownloadDriver(driverFileName, browserVersion);
                if (success) {
                    paths = FileLocator.listRegularFilesRecursiveMatchedToName(driverDir, 3, driverFileName);
                    driverPath = checkDriverVersionWithBrowser(paths, browserVersion);
                } else {
                    throw new ApollonBaseException(CUSTOM_MESSAGE, "Failed on downloading driver!");
                }
            }
            if (driverPath != null) {
                trace("Find Driver: " + driverPath.toFile().getName() + " with Version: " + browserVersion);
                return driverPath;
            } else {
                throw new ApollonBaseException(CUSTOM_MESSAGE, "No matched driver for current test browser was found or downloaded!");
            }
        }
    }


    /**
     * check driver version against browser version
     *
     * @param paths          file paths of driver files
     * @param browserVersion Browser version
     * @return path of matched driver with major version number
     */
    private static Path checkDriverVersionWithBrowser(List<Path> paths, String browserVersion) {
        Path driverPath = null;
        for (Path path : paths) {
            driverPath = checkVersion(path, browserVersion);
            if (driverPath != null) {
                break;
            }
        }
        return driverPath;
    }

    /**
     * check single driver with given browser version
     *
     * @param path           driver path
     * @param browserVersion browser Version
     * @return Path of driver if match else null
     */
    private static Path checkVersion(Path path, String browserVersion) {
        //check driver version with filepath --v
        String[] response = executeCommand(path.toString() + " --v");
        if (Integer.parseInt(response[0]) != 0) {
            warn("Failed on check Driver version: " + response[1]);
        } else {
            Matcher matcher = pattern.matcher(response[1]);
            if (matcher.find()) {
                String driverVersion = matcher.group(1);
                //match Chrome version with existing driver and return driver path
                if (driverVersion.equals(browserVersion)) {
                    trace("Find match driver: " + path);
                    return path;
                } else {
                    warn("Check Given Driver: <" + path + "> has Version: " + response[1]
                            + ", but not match to Browser Version: " + browserVersion);
                }
            } else {
                warn("Version can not be recognized with given pattern in: " + response[1]);
            }
        }
        return null;
    }

    /**
     * get current chrome version
     *
     * @return version in string
     */
    public static String getCurrentChromeVersion() {
        String[] response = null;
        if (PropertyResolver.isWindows()) {
            response = executeCommand("reg query \"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\" /v version");
        } else if (PropertyResolver.isLinux()) {
            response = executeCommand("google-chrome --version");
        } else if (PropertyResolver.isMac()) {
            response = executeCommand("/Applications/Google\\ Chrome.app/Contents/MacOS/Google\\ Chrome --version");
        } else {
            throw new ApollonBaseException(CUSTOM_MESSAGE, "Unsupported Operation System: " + PropertyResolver.getProperty("os.name"));
        }
        if (Integer.parseInt(response[0]) != 0) {
            throw new ApollonBaseException(CUSTOM_MESSAGE, "Check Version went Wrong!\n" + response[1]);
        }
        Matcher matcher = pattern.matcher(response[1]);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            warn("No Version was found with given Pattern -> " + response[1]);
            return response[1];
        }
    }


    /**
     * get current Edge version
     *
     * @return major version in string
     */
    public static String getCurrentEdgeVersion() {
        String[] response = null;
        if (PropertyResolver.isWindows()) {
            response = executeCommand("reg query \"HKEY_CURRENT_USER\\Software\\Microsoft\\Edge\\BLBeacon\" /v version");
        } else if (PropertyResolver.isLinux()) {
            response = executeCommand("microsoft-edge --version");
        } else if (PropertyResolver.isMac()) {
            response = executeCommand("/Applications/Microsoft\\ Edge.app/Contents/MacOS/Microsoft\\ Edge --version");
        } else {
            throw new ApollonBaseException(CUSTOM_MESSAGE, "Unsupported Operation System: " + PropertyResolver.getProperty("os.name"));
        }
        if (Integer.parseInt(response[0]) != 0) {
            throw new ApollonBaseException(CUSTOM_MESSAGE, "Check Version went Wrong!\n" + response[1]);
        }
        Matcher matcher = pattern.matcher(response[1]);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            warn("No Version was found with given Pattern -> " + response[1]);
            return response[1];
        }
    }

    /**
     * download chrome drivers from TFS resources or drivers.zip in share folder
     *
     * @return folder of drivers
     */
    public static boolean tryToDownloadDriver(String driverFileName, String browserVersion) {
        String downloadFolder = PropertyResolver.getDriverResourceLocation();
        if (downloadFolder.startsWith("http")) {
            //todo: download from server
            throw new ApollonBaseException(DOWNLOAD_WEB_DRIVER_NOT_DEVELOPED_YET);
        } else if (downloadFolder.toLowerCase().startsWith("git")) {
            return downloadFromTFS(downloadFolder, driverFileName, browserVersion);
        } else if (downloadFolder.startsWith("\\\\")) {
            File driverFile = new File(downloadFolder + "\\drivers.zip");
            String filePath = FileLocator.getProjectBaseDir() + "/src/main/resources/" + PropertyResolver.getWebDriverBinLocation() + "/drivers.zip";
            File target = new File(filePath);
            trace("Write to target: " + target.getAbsolutePath());
            FileOperation.copyFileTo(driverFile.toPath(), target.toPath());
            unzipAndDelete(target);
            return true;
        }
        return false;
    }

    /**
     * download driver from tfs: tfvc items
     *
     * @param folder target folder
     * @return folder of downloaded local file
     */
    private static boolean downloadFromTFS(String folder, String fileName, String browserVersion) {
        if (!PropertyResolver.isTFSConnectEnabled()) {
            warn("Failed on download Driver on TFS Server: Connection disabled! Please set to enabled if necessary!");
        } else {
            JSONRunnerConfig runnerConfig = JSONContainerFactory.getRunnerConfig(PropertyResolver.getTFSRunnerConfigFile());
            Map<String, String> config = runnerConfig.getTfsConfig();
            TFSRestClient tfsRestClient = new TFSRestClient(config);
            String parentFolder = FileLocator.getProjectBaseDir() + "/src/main/resources/"
                    + PropertyResolver.getWebDriverBinLocation();
            //try to look up the driver and download it
            Map<Integer, String> items = tfsRestClient.getItemsMap(folder, fileName, true);
            for (Map.Entry<Integer, String> entry : items.entrySet()) {
                String value = entry.getValue();
                File driverFile = new File(parentFolder + FileOperation.getFileName(value));
                tfsRestClient.downloadFile(value, driverFile);
                Path filePath = checkVersion(driverFile.toPath(), browserVersion);
                if (filePath != null) {
                    return true;
                } else {
                    trace("Driver removed...");
                    driverFile.deleteOnExit();
                }
            }
        }
        return false;
    }

    private static void unzipAndDelete(File zipFile) {
        ZipUtils.unzipFileHere(zipFile);
        zipFile.deleteOnExit();
    }

    private static void sleep(int sec) {
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
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return FindImageInImage.findSubImage(subImage, subWidth, subHeight, UserRobot.captureMainFullScreen(), screenSize.width, screenSize.height, allowedPixelFailsPercent, allowedPixelColorDifference);
    }
} 
