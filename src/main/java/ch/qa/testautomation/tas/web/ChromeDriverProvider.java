package ch.qa.testautomation.tas.web;

import ch.qa.testautomation.tas.configuration.PropertyResolver;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;

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
        if (Objects.nonNull(options)) {
            tempOptions = tempOptions.merge(options);
        }
        ChromeDriver chromeDriver = new ChromeDriver(tempOptions);
        chromeDriver.manage().window().setPosition(new Point(0, 0));
        configureWindowSize(chromeDriver, isChromeMaximised());
        setDriver(chromeDriver);
    }

    public DevTools getDevTools() {
        return ((ChromeDriver) getDriver()).getDevTools();
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (!PropertyResolver.getBrowserBinPath().isEmpty()) {
            options.setBinary(new File(PropertyResolver.getBrowserBinPath()));
        }
        options.addArguments("--no-sandbox")
                .addArguments("--lang=de")
                .addArguments("disable-infobars")
                .addArguments("--disable-web-security")
                .addArguments("--allow-running-insecure-content")
                .addArguments("--disable-dev-shm-usage")
                .addArguments("--remote-allow-origins=*")
                .setAcceptInsecureCerts(true);
        if (isChromeMaximised()) {
            options.addArguments("start-maximized");
        }
        if (isHeadlessChrome()) {
            options.addArguments("--headless")
                    .addArguments("--allowed-ips=*");
        }
        configurePosition(options);
        options.setExperimentalOption("prefs", getChromePrefs());
        return options;
    }

    private static Map<String, Object> getChromePrefs() {
        Map<String, Object> chromePrefs = new HashMap<>();
        String dPath = PropertyResolver.getDownloadDir();
        chromePrefs.put("download.default_directory", dPath);
        if (!new File(dPath).exists() && new File(dPath).mkdirs()) {
            info("Download Directory not exists! Make Dirs: " + dPath);
        }
        chromePrefs.put("download.prompt_for_download", false);
        chromePrefs.put("download.open_pdf_in_system_reader", PropertyResolver.isOpenPDFInSystemReader());
        chromePrefs.put("plugins.always_open_pdf_externally", true);
        chromePrefs.put("autofill.profile_enabled", false);

        if (isHeadlessChrome()) {
            chromePrefs.put("safebrowsing.enabled", false);
            chromePrefs.put("profile.default_content_settings.popups", 0);
        }
        return chromePrefs;
    }

    private static boolean isHeadlessChrome() {
        return PropertyResolver.isHeadlessModeEnabled();
    }

    private static boolean isChromeMaximised() {
        return PropertyResolver.getBrowserFullscreenEnabled();
    }

    private static void configurePosition(ChromeOptions options) {
        String position = System.getProperty("WebDriverPosition");
        if (position != null) {
            options.addArguments("window-position=" + position);
        }
    }

}
