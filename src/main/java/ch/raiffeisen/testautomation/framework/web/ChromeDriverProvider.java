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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class ChromeDriverProvider extends WebDriverProvider {

    /**
     * init chrome driver with options
     */
    public void initialize() {
        ChromeDriver chromeDriver = new ChromeDriver(getChromeOptions());

        if (!isChromeMaximised()) {
            String[] point = System.getProperty("WebDriverPosition", "0,0").split(",");
            String[] size = System.getProperty("WebDriverSize", "1920,1080").split(",");
            Point po = new Point(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
            Dimension di = new Dimension(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
            chromeDriver.manage().window().setPosition(po);
            chromeDriver.manage().window().setSize(di);
        }
        chromeDriver.manage().timeouts().implicitlyWait(6, TimeUnit.SECONDS);
        setDriver(chromeDriver);
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox")
                .addArguments("--disable-infobars")
                .addArguments("test-type")
                .addArguments("--lang=de-CH")
                .setExperimentalOption("useAutomationExtension", false);
        if (isChromeMaximised()) {
            options.addArguments("--start-maximized");
            options.addArguments("--window-size=1920,1080");
        }
        configureHeadlessChrome(options);
        configurePositionAndSize(options);
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
        }
    }

    private static void configurePositionAndSize(ChromeOptions options) {
        String size = System.getProperty("WebDriverSize");
        if (size != null) {
            options.addArguments("window-size=" + size);
        }
        String position = System.getProperty("WebDriverPosition");
        if (position != null) {
            options.addArguments("window-position=" + position);
        }
    }

    public static RemoteWebDriver createDriverFromSession(final String sessionId, URL url, Capabilities capabilities) {
        CommandExecutor executor = new HttpCommandExecutor(url) {
            @Override
            public Response execute(Command command) throws IOException {
                Response response = null;
                if (command.getName().equals("newSession")) {
                    response = new Response();
                    response.setSessionId(sessionId);
                    response.setStatus(0);
                    response.setValue(Collections.<String, String>emptyMap());
                    try {
                        Field commandCodec = this.getClass().getSuperclass().getDeclaredField("commandCodec");
                        commandCodec.setAccessible(true);
                        commandCodec.set(this, new W3CHttpCommandCodec());

                        Field responseCodec = null;
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
