package io.github.sleod.tas.web;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.github.sleod.tas.common.utils.DateTimeUtils;
import io.github.sleod.tas.configuration.PropertyResolver;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class PlaywrightDriver {
    @Getter
    private final BrowserContext context;
    @Setter
    private Page page;
    @Getter
    private String videoDirectory;

    public PlaywrightDriver(Playwright playwright) {
        playwright.selectors().setTestIdAttribute(PropertyResolver.getTestIdAttribute());
        this.context = playwright.chromium().launchPersistentContext(ChromeUserPreference.getUserDataDir(), getBrowserLauchOptions());
    }

    private BrowserType.LaunchPersistentContextOptions getBrowserLauchOptions() {
        BrowserType.LaunchPersistentContextOptions launchOptions = new BrowserType.LaunchPersistentContextOptions()
                .setChannel(PropertyResolver.getUsedBrowserName()).setHeadless(PropertyResolver.isHeadlessModeEnabled())
                .setChromiumSandbox(false)
                .setDownloadsPath(new File(PropertyResolver.getDownloadDir()).toPath())
                .setTimeout(PropertyResolver.getDriverWaitTimeout())
                .setDownloadsPath(new File(PropertyResolver.getDownloadDir()).toPath())
                .setLocale("de-CH")
                .setAcceptDownloads(true)
                .setIgnoreHTTPSErrors(true)
                .setViewportSize(PropertyResolver.getBrowserScreenWidth(), PropertyResolver.getBrowserScreenHigh())
                .setArgs(getArguments());
        String chromePath = PropertyResolver.getBrowserBinPath();
        if (!chromePath.isEmpty()) {
            launchOptions.setExecutablePath(Paths.get(chromePath));
        }
        if (PropertyResolver.isGenerateVideoEnabled()) {
            videoDirectory = "/target/generated-videos/" + DateTimeUtils.getNowMilli();
            launchOptions.setRecordVideoDir(Paths.get(videoDirectory));
        }
        return launchOptions;
    }

    private List<String> getArguments() {
        List<String> arguments = new LinkedList<>();
        arguments.add("--remote-allow-origins=*");
        arguments.add("--disable-web-security");
        arguments.add("--disable-dev-shm-usage");
        arguments.add("--disable-infobars");
        arguments.add("--disable-web-security");
        arguments.add("--allow-running-insecure-content");
        arguments.add("--no-cache");
        return arguments;
    }

    public File screenshot(boolean fullPage) {
        File screenshotFile = new File("target/temp/screenshot_" + DateTimeUtils.getNowMilli() + ".png");
        screenshotFile.getParentFile().mkdirs();
        getPage().screenshot(new Page.ScreenshotOptions().setFullPage(fullPage).setPath(screenshotFile.toPath()));
        return screenshotFile;
    }

    public void open(String url) {
        getPage().navigate(url);
    }

    public List<Page> allPages() {
        return getContext().pages();
    }

    public File getVideoFile() {
        return getPage().video().path().toFile();
    }

    public PlaywrightWebElement find(String selector) {
        return new PlaywrightWebElement(getPage(), selector);
    }

    /**
     * Switches to a tab by its title.
     *
     * @param title The title of the tab to switch to.
     * @return The Page object of the target tab, or null if not found.
     */
    public Page switchToTabByTitle(String title) {
        for (Page page : getContext().pages()) {
            if (title.equals(page.title())) {
                setPage(page);
                return getPage();
            }
        }
        return null; // Tab with the specified title not found
    }

    public void closePage(String title) {
        switchToTabByTitle(title).close();
        setPage(null);
    }

    public Page newPage() {
        setPage(getContext().newPage());
        return getPage();
    }

    public Page getPage() {
        if (page == null) {
            if (getContext().pages().isEmpty()) {
                page = newPage();
            } else {
                page = getContext().pages().getFirst();
            }
        }
        return page;
    }

    public void quit() {
        getPage().close();
        getContext().close();
    }

}