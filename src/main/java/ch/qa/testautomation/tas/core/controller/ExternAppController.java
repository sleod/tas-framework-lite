package ch.qa.testautomation.tas.core.controller;

import ch.qa.testautomation.tas.common.IOUtils.FileLocator;
import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.common.enumerations.DownloadStrategy;
import ch.qa.testautomation.tas.common.utils.ZipUtils;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.*;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;
import static ch.qa.testautomation.tas.exception.ExceptionErrorKeys.CUSTOM_MESSAGE;
import static java.util.Objects.nonNull;

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
            throw new ExceptionBase(CUSTOM_MESSAGE, "Empty Keys or Key Combination more than 3 keys is not provided yet!");
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
        int exitCode = 0;
        StringJoiner output;
        try {
            if (PropertyResolver.isWindows()) {
                builder.command("cmd.exe", "/c", command);
            } else {
                builder.command("sh", "-c", command);
            }
            debug("Executing command: " + command);
            Process process = builder.start();
            exitCode = process.waitFor();

            output = createOutput(process);
            debug("Output: " + output);
        } catch (IOException | InterruptedException ex) {
            throw new ExceptionBase(CUSTOM_MESSAGE, ex, "Exception while execute command line: " + command);
        }
        return new String[]{String.valueOf(exitCode), output.toString()};
    }

    public static StringJoiner createOutput(Process process) throws IOException {
        StringJoiner output = new StringJoiner("\n");
        StringJoiner errorOutput = new StringJoiner("\n");
        int exitValue = process.exitValue();

        debug("Process exit code: " + exitValue);

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            output.add(line);
        }

        if (exitValue > 0) {
            errorOutput.add("Error Message: ");
            BufferedReader readerError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorLine;
            while ((errorLine = readerError.readLine()) != null) {
                errorOutput.add(errorLine);
            }
            error(errorOutput.toString());
            output.add(errorOutput.toString());
        }

        return output;
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
    public static Path matchBrowserAndDriverVersion(String browserVersion, String driverFileName) {
        File driverFile = new File(PropertyResolver.getWebDriverBinLocation());//direct input absolut filepath
        if (driverFile.exists() && driverFile.isFile() && nonNull(checkVersion(driverFile.toPath(), browserVersion))) {
            return driverFile.toPath();
        } else {
            //scan driver in local folder
            String driverDir = FileLocator.getProjectBaseDir() + "/src/main/resources/" + PropertyResolver.getWebDriverBinLocation();
            new File(driverDir).mkdirs();
            List<Path> paths = FileLocator.listRegularFilesRecursiveMatchedToName(driverDir, 3, driverFileName);
            Path driverPath = null;
            if (!paths.isEmpty()) {//in case found existing local driver files, check version
                driverPath = checkDriverVersionWithBrowser(paths, browserVersion);
            }
            if (paths.isEmpty() || driverPath == null) {//in case existing not match or nothing found, try to download
                info("No local driver matched to current browser! Try to download driver!");
                if (tryToDownloadDriver()) {
                    paths = FileLocator.listRegularFilesRecursiveMatchedToName(driverDir, 3, driverFileName);
                    driverPath = checkDriverVersionWithBrowser(paths, browserVersion);
                }
            }
            if (driverPath != null) {
                info("Find Driver: " + driverPath.toFile().getName() + " with Version: " + browserVersion);
                return driverPath;
            } else {
                throw new ExceptionBase(CUSTOM_MESSAGE, "No matched driver for current test browser was found or downloaded!");
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
    public static Path checkDriverVersionWithBrowser(List<Path> paths, String browserVersion) {
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
    public static Path checkVersion(Path path, String browserVersion) {
        //check driver version with filepath --v
        grantPermission(path, 755);
        String[] response = executeCommand(path.toString() + " --v");
        if (Integer.parseInt(response[0]) != 0) {
            info("Failed on check Driver version: " + response[1]);
        } else {
            Matcher matcher = pattern.matcher(response[1]);
            if (matcher.find()) {
                String driverVersion = matcher.group(1);
                //match Chrome version with existing driver and return driver path
                if (driverVersion.equals(browserVersion)) {
                    PropertyResolver.setBrowserVersion(matcher.group());
                    info("Find match driver: " + path);
                    return path;
                } else {
                    debug("Check Given Driver: <" + path + "> has Version: " + response[1]
                            + ", but not match to Browser Version: " + browserVersion);
                }
            } else {
                debug("Version can not be recognized with given pattern in: " + response[1]);
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
        String[] response;
        if (PropertyResolver.isWindows()) {
            response = executeCommand("reg query \"HKEY_CURRENT_USER\\Software\\Google\\Chrome\\BLBeacon\" /v version");
        } else if (PropertyResolver.isLinux()) {
            response = executeCommand("google-chrome --version");
        } else if (PropertyResolver.isMac()) {
            response = executeCommand("/Applications/Google\\ Chrome.app/Contents/MacOS/Google\\ Chrome --version");
        } else {
            throw new ExceptionBase(CUSTOM_MESSAGE, "Unsupported Operation System: " + PropertyResolver.getProperty("os.name"));
        }
        return processResponse(response);
    }

    /**
     * get current Edge version
     *
     * @return major version in string
     */
    public static String getCurrentEdgeVersion() {
        String[] response;
        if (PropertyResolver.isWindows()) {
            response = executeCommand("reg query \"HKEY_CURRENT_USER\\Software\\Microsoft\\Edge\\BLBeacon\" /v version");
        } else if (PropertyResolver.isLinux()) {
            response = executeCommand("microsoft-edge --version");
        } else if (PropertyResolver.isMac()) {
            response = executeCommand("/Applications/Microsoft\\ Edge.app/Contents/MacOS/Microsoft\\ Edge --version");
        } else {
            throw new ExceptionBase(CUSTOM_MESSAGE, "Unsupported Operation System: " + PropertyResolver.getProperty("os.name"));
        }
        return processResponse(response);
    }

    public static String processResponse(String[] response) {
        if (Integer.parseInt(response[0]) != 0) {
            if (response[1].contains("not found")) {
                warn("No Browser found locally. Need be downloaded!");
                response[0] = "0";
                response[1] = getChromeLatestStableVersion();
            } else {
                throw new ExceptionBase(CUSTOM_MESSAGE, "Check Version went Wrong!\n" + response[1]);
            }
        }
        Matcher matcher = pattern.matcher(response[1]);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            info("No Version was found with given Pattern -> " + response[1]);
            return response[1];
        }
    }

    /**
     * download chrome drivers from TFS resources or drivers.zip in share folder
     *
     * @return folder of drivers
     */
    public static boolean tryToDownloadDriver() {
        DownloadStrategy strategy = DownloadStrategy.valueOf(PropertyResolver.getDownloadStrategy().toUpperCase());
        JsonNode config = getDownloadConfig();
        switch (strategy) {
            case ONLINE -> {
                return downloadDriverOnline(config);
            }
            case SHARED_FILE -> {
                return downloadSharedDriver(config);
            }
            case AUTO -> {
                info("Try to download driver with auto detection mode...");
                boolean success = downloadSharedDriver(config);
                if (!success) {
                    success = downloadDriverOnline(config);
                }
                return success;
            }
        }
        return false;
    }

    public static JsonNode getDownloadConfig() {
        if (!FileOperation.isFileExists(PropertyResolver.getDriverDownloadConfigFile())) {
            throw new ExceptionBase(CUSTOM_MESSAGE, "Download Config File not exist! -> " + PropertyResolver.getDriverDownloadConfigFile());
        }
        return JSONContainerFactory.getConfig(PropertyResolver.getDriverDownloadConfigFile());
    }

    public static boolean downloadDriverOnline(JsonNode config) {
        info("Try to download driver online.");
        try {
            String downloadLink = config.get("downloadLink").asText();
            String platform = config.get("platform").asText();
            String browser = config.get("browser").asText();
            String version = getChromeLatestStableVersion();
            info("Current Stable Version: " + version);
            if (config.get("installBrowser").asBoolean()) {
                String BROWSER_FILE_URL = downloadLink + version + "/" + platform + "/" + browser + "-" + platform + ".zip";
                String location = FileLocator.getProjectBaseDir() + "/src/main/resources/" + PropertyResolver.getWebDriverBinLocation();
                new File(location).mkdirs();
                String filePath = location + "/" + browser + "-" + platform + ".zip";
                downloadFromURL(BROWSER_FILE_URL, filePath);
                unzipAndDelete(new File(filePath));
                setCurrentBrowser(location, browser);
            } else {
                String currentChromeVersion = getCurrentChromeVersion();
                if (!version.startsWith(currentChromeVersion)) {
                    debug("Online Stable Chrome Driver Version: " + version + " does not match current Chrome version: " + currentChromeVersion + ". Download Aborted!");
                    return false;
                }
            }
            String DRIVER_FILE_URL = downloadLink + version + "/" + platform + "/" + browser + "driver" + "-" + platform + ".zip";
            String filePath = FileLocator.getProjectBaseDir() + "/src/main/resources/" + PropertyResolver.getWebDriverBinLocation() + "/" + browser + "driver" + "-" + platform + ".zip";
            downloadFromURL(DRIVER_FILE_URL, filePath);
            unzipAndDelete(new File(filePath));
            return true;
        } catch (Exception ex) {
            debug("Fail on download driver online! " + ex.getMessage());
            return false;
        }
    }

    private static void setCurrentBrowser(String location, String browser) {
        Path browserBinPath = FileLocator.findExactFile(location, 5, PropertyResolver.isWindows() ? browser + ".exe" : browser);
        //set chrome bin file path - chromeOption
        PropertyResolver.setBrowserBinPath(browserBinPath.toFile().getAbsolutePath());
        grantPermission(browserBinPath, 755);
    }

    private static String getChromeLatestStableVersion() {
        JsonNode config = getDownloadConfig();
        if (DownloadStrategy.ONLINE.equals(DownloadStrategy.valueOf(PropertyResolver.getDownloadStrategy().toUpperCase()))) {
            ObjectMapper mapper = new ObjectMapper();
            String url = config.get("lastKnownGoodVersions").asText();
            JsonNode jsonNode;
            try {
                jsonNode = mapper.readTree(new URL(url));
            } catch (IOException ex) {
                throw new ExceptionBase(CUSTOM_MESSAGE, ex, "Exception by get json from google! -> " + url);
            }
            return jsonNode.get("channels").get("Stable").get("version").asText();
        } else return config.get("version").asText();

    }

    public static boolean downloadSharedDriver(JsonNode config) {
        info("Try to download with shared file link for driver.");
        String sharedFileLink = config.get("sharedFileLink").asText();
        if (!isValid(sharedFileLink)) {
            debug("Shared File Link of Driver was not found in Config!");
            return false;
        }
        String filePath = FileLocator.getProjectBaseDir() + "/src/main/resources/" + PropertyResolver.getWebDriverBinLocation() + "/drivers.zip";
        copySharedResource(sharedFileLink + "/drivers.zip", filePath);
        if (config.get("installBrowser").asBoolean()) {
            String location = FileLocator.getProjectBaseDir() + "/src/main/resources/" + PropertyResolver.getWebDriverBinLocation();
            filePath = location + "/browser.zip";
            String browser = config.get("browser").asText();
            String browserPath = sharedFileLink + "/" + browser + "-" + config.get("platform").asText() + ".zip";
            copySharedResource(browserPath, filePath);
            setCurrentBrowser(location, browser);
        }
        return true;
    }

    public static void copySharedResource(String resourcePath, String filePath) {
        File targetFile = new File(resourcePath);
        File target = new File(filePath);
        debug("Write to target: " + target.getAbsolutePath());
        FileOperation.copyFileTo(targetFile.toPath(), target.toPath());
        unzipAndDelete(target);
    }

    public static void grantPermission(Path browserBinPath, int code) {
        if (!PropertyResolver.isWindows()) {
            executeCommand("chmod " + code + " " + browserBinPath.toFile().getAbsolutePath());
        }
    }

    public static void downloadFromURL(String fileUrl, String filePath) {
        info("Try to download driver with URL: " + fileUrl);
        File target = new File(filePath);
        try (BufferedInputStream in = new BufferedInputStream(new URL(fileUrl).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(target.getPath())) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        } catch (IOException ex) {
            // handle exception
            throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_WRITING, ex, "Exception by downloading file.");
        }
    }

    public static void unzipAndDelete(File zipFile) {
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
} 
