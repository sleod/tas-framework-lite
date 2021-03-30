package ch.raiffeisen.testautomation.framework.web;

import ch.raiffeisen.testautomation.framework.common.logging.SystemLogger;
import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.http.W3CHttpCommandCodec;
import org.openqa.selenium.remote.http.W3CHttpResponseCodec;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ChromeDriverProvider extends WebDriverProvider {

    /**
     * init chrome driver with options
     */
    public void initialize() {
        ChromeDriver chromeDriver = new ChromeDriver(getChromeOptions());
        chromeDriver.manage().window().setPosition(new Point(0, 0));
        chromeDriver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);
        configureWindowSize(chromeDriver);
        setDriver(chromeDriver);
    }

    private static void configureWindowSize(ChromeDriver driver) {

        if (isChromeMaximised()) {
            driver.manage().window().maximize();
        } else {
            String[] dimensions = PropertyResolver.getScreenSize().split(",");
            driver.manage().window().setSize(new Dimension(Integer.parseInt(dimensions[0]),Integer.parseInt(dimensions[1])));

        }
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox")
                .addArguments("--disable-infobars")
                .addArguments("--disable-web-security")
                .addArguments("--allow-running-insecure-content")
                .addArguments("--test-type")
                .addArguments("--lang=de");
//                .setExperimentalOption("useAutomationExtension", false);
        configureHeadlessChrome(options);
        configurePosition(options);
        return options;
    }

    public static boolean isHeadlessChrome() {
        return PropertyResolver.useHeadlessChrome();
    }

    public static boolean isChromeMaximised() {
        return PropertyResolver.useMaximised();
    }

    private static void configureHeadlessChrome(ChromeOptions options) {
        if (isHeadlessChrome()) {
            options.addArguments("headless");
            options.addArguments("disable-gpu");
            HashMap<String, Object> chromePrefs = new HashMap<>();
            chromePrefs.put("safebrowsing.enabled", "false");
            chromePrefs.put("profile.default_content_settings.popups", 0);
            chromePrefs.put("download.prompt_for_download", "false");
            chromePrefs.put("download.default_directory", Paths.get(System.getProperty("user.home"), "Downloads"));
            if (!PropertyResolver.getDefaultDownloadDir().isEmpty()) {
                String dir = PropertyResolver.getDefaultDownloadDir();
                chromePrefs.put("download.default_directory", dir);
                if (!new File(dir).exists() && new File(dir).mkdirs()) {
                    SystemLogger.warn("Download Directory not exists! Make Dirs: " + dir);
                }
            }
            options.setExperimentalOption("prefs", chromePrefs);
        }
    }

    private static void configurePosition(ChromeOptions options) {

        String position = System.getProperty("WebDriverPosition");
        if (position != null) {
            options.addArguments("window-position=" + position);
        }
    }

    public static RemoteWebDriver createDriverFromSession(final String sessionId, URL url, Capabilities capabilities) {
        CommandExecutor executor = new HttpCommandExecutor(url) {
            @Override
            public Response execute(Command command) throws IOException {
                Response response;
                if (command.getName().equals("newSession")) {
                    response = new Response();
                    response.setSessionId(sessionId);
                    response.setStatus(0);
                    response.setValue(Collections.<String, String>emptyMap());
                    try {
                        Field commandCodec = this.getClass().getSuperclass().getDeclaredField("commandCodec");
                        commandCodec.setAccessible(true);
                        commandCodec.set(this, new W3CHttpCommandCodec());

                        Field responseCodec;
                        responseCodec = this.getClass().getSuperclass().getDeclaredField("responseCodec");
                        responseCodec.setAccessible(true);
                        responseCodec.set(this, new W3CHttpResponseCodec());
                    } catch (NoSuchFieldException | IllegalAccessException ex) {
                        SystemLogger.error(ex);
                    }
                } else {
                    response = super.execute(command);
                }
                return response;
            }
        };
        return new RemoteWebDriver(executor, capabilities);
    }
}
