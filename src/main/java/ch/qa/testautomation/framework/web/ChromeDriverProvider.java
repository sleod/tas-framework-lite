package ch.qa.testautomation.framework.web;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.info;

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
        tempOptions.setPlatformName(System.getProperty("os.name"));
        if (options != null) {
            tempOptions.merge(options);
        }
        chromeDriver = new ChromeDriver(tempOptions);
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
                .addArguments("--save-page-as-mhtml")//save page as mhtml in one file
                .addArguments("--test-type")
                .addArguments("--lang=de");
        if (isChromeMaximised()) {
            options.addArguments("start-maximized");
        }
        if (isHeadlessChrome()) {
            options.addArguments("headless");
            options.addArguments("disable-gpu");
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
        if (isHeadlessChrome()) {
            chromePrefs.put("safebrowsing.enabled", false);
            chromePrefs.put("profile.default_content_settings.popups", 0);
        }
        return chromePrefs;
    }

    private static boolean isHeadlessChrome() {
        return PropertyResolver.useHeadlessMode();
    }

    private static boolean isChromeMaximised() {
        return PropertyResolver.useMaximised();
    }

    private static void configurePosition(ChromeOptions options) {
        String position = System.getProperty("WebDriverPosition");
        if (position != null) {
            options.addArguments("window-position=" + position);
        }
    }

}
