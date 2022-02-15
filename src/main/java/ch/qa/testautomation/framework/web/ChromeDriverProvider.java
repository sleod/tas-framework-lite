package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.*;
import org.openqa.selenium.remote.codec.w3c.W3CHttpCommandCodec;
import org.openqa.selenium.remote.codec.w3c.W3CHttpResponseCodec;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;

public class ChromeDriverProvider extends WebDriverProvider {
    private final ChromeOptions options;

    public ChromeDriverProvider() {
        options = null;
    }

    public ChromeDriverProvider(ChromeOptions options) {
        this.options = options;
    }

    /**
     * init chrome driver with options
     */
    public void initialize() {
        ChromeOptions tempOptions = getChromeOptions();
        ChromeDriver chromeDriver;
        if (options == null) {
            chromeDriver = new ChromeDriver(tempOptions);
        } else {
            chromeDriver = new ChromeDriver(tempOptions.merge(options));
        }
        chromeDriver.manage().window().setPosition(new Point(0, 0));
        chromeDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(6));
        configureWindowSize(chromeDriver, isChromeMaximised());
        setDriver(chromeDriver);
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox")
                .addArguments("--disable-infobars")
                .addArguments("--disable-web-security")
                .addArguments("--allow-running-insecure-content")
                .addArguments("--test-type")
                .addArguments("--lang=de");
        configureHeadlessChrome(options);
        configurePosition(options);
        return options;
    }

    public static boolean isHeadlessChrome() {
        return PropertyResolver.useHeadlessMode();
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
