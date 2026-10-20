package io.github.sleod.tas.web;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import io.github.sleod.tas.common.utils.DateTimeUtils;
import io.github.sleod.tas.configuration.PropertyResolver;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;


public class PlaywrightDriver {
    static final String ARG_DISABLE_AUTOMATION_CONTROLLED = "--disable-blink-features=AutomationControlled";
    static final String ARG_TEST_TYPE = "--test-type=webdriver";
    static final String DEFAULT_ARG_ENABLE_AUTOMATION = "--enable-automation";
    @Getter
    private final BrowserContext context;
    @Setter
    private Page page;
    @Getter
    private String videoDirectory;

    public PlaywrightDriver(Playwright playwright) {
        playwright.selectors().setTestIdAttribute(PropertyResolver.getTestIdAttribute());
        this.context = playwright.chromium().launchPersistentContext(BrowserUserPreference.getUserDataDir(), getBrowserLauchOptions());
        //Set all wait times for Playwright
        int driverWaitTimeout = PropertyResolver.getDriverWaitTimeout();
        context.setDefaultTimeout(driverWaitTimeout);
        context.setDefaultNavigationTimeout(driverWaitTimeout);
        PlaywrightAssertions.setDefaultAssertionTimeout(driverWaitTimeout);

        setUpDemoMode();
    }

    private void setUpDemoMode() {
        if (PropertyResolver.isDemoModeEnabled()) {
            context.addInitScript("""
                    (() => {
                        const flash = (el) => {
                            if (!(el instanceof Element)) return;
                            const original = el.style.outline;
                            el.style.outline = '3px solid red';
                            setTimeout(() => {
                                el.style.outline = original;
                            }, 500);
                        };
                        ['click', 'contextmenu', 'dblclick', 'focusin', 'change', 'input', 'submit']
                        .forEach(type => window.addEventListener(type, e => flash(e.target), true));
                    })();
                    """);
        }
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
        if (PropertyResolver.isHideAutomationBannerEnabled()) {
            launchOptions.setIgnoreDefaultArgs(getIgnoredDefaultArguments());
        }
        String chromePath = PropertyResolver.getBrowserBinPath();
        if (!chromePath.isEmpty()) {
            launchOptions.setExecutablePath(Paths.get(chromePath));
        }
        if (PropertyResolver.isGenerateVideoEnabled()) {
            videoDirectory = PropertyResolver.getVideoLocation() + DateTimeUtils.getNowMilli();
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
        arguments.add("--disable-gpu");
        arguments.add("--disable-features=msEdgeOnRampFRE");
        arguments.add("--disable-features=EdgeFirstRunExperience");
        arguments.add("--no-first-run");
        if (PropertyResolver.isHideAutomationBannerEnabled()) {
            arguments.add(ARG_DISABLE_AUTOMATION_CONTROLLED);
            //without "--enable-automation" chrome shows a warning bar for unsupported flags like "--no-sandbox".
            //"--test-type" suppresses this bar the same way as "--enable-automation" did, but without the automation banner.
            arguments.add(ARG_TEST_TYPE);
        }
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

    public Page getCurrentTab() {
        for (Page p : context.pages()) {
            Boolean hasFocus = (Boolean) p.evaluate("() => document.hasFocus()");
            if (Boolean.TRUE.equals(hasFocus)) {
                return p;
            }
        }
        return getPage();
    }

    /**
     * Playwright starts chromium with "--enable-automation" by default which shows the automation
     * info banner. The banner can only be removed by ignoring this default argument.
     *
     * @return list of playwright default arguments to be ignored on browser start
     */
    private static List<String> getIgnoredDefaultArguments() {
        return List.of(DEFAULT_ARG_ENABLE_AUTOMATION);
    }

}