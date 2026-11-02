package com.servet.hybrid.framework.web;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.servet.hybrid.framework.config.Env;

/**
 * One browser per scenario (PicoContainer), started lazily: only when a step really needs a page.
 * An API-only scenario therefore never opens a browser — without any tag or extra hook.
 */
public class Browser {

    public static final AtomicInteger STARTED = new AtomicInteger();

    private WebDriver driver;
    private String session;

    public WebDriver driver() {
        if (driver == null) {
            driver = start();
            STARTED.incrementAndGet();
        }
        return driver;
    }

    public boolean isStarted() {
        return driver != null;
    }

    /**
     * The hybrid shortcut: log in through the API, put the session into the app's localStorage and
     * open the page directly — no login form in every scenario. localStorage belongs to an origin,
     * so the app is opened once before the value can be written.
     * <p>
     * The value is written only once per session: an app may rotate it on first use (Musify replaces
     * its refresh token), and writing the old value again would be a replay that the server rejects.
     */
    public void openWithSession(String appUrl, String storageKey, String storageValue, String path) {
        WebDriver browser = driver();
        String wanted = appUrl + "|" + storageKey + "|" + storageValue;
        if (!wanted.equals(session)) {
            browser.get(appUrl + "/");
            ((JavascriptExecutor) browser).executeScript("window.localStorage.setItem(arguments[0], arguments[1]);",
                    storageKey, storageValue);
            session = wanted;
        }
        browser.get(appUrl + path);
    }

    public void quit() {
        if (driver != null) {
            driver.quit();
            driver = null;
            session = null;
        }
    }

    /** Starting several Chromes at once sometimes fails on Windows: only the start is tried a second time. */
    private static WebDriver start() {
        try {
            return new ChromeDriver(options());
        } catch (SessionNotCreatedException firstAttempt) {
            return new ChromeDriver(options());
        }
    }

    private static ChromeOptions options() {
        ChromeOptions options = new ChromeOptions();
        if (Env.flag("browser.headless")) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1600,1000");
        options.setExperimentalOption("prefs", Map.of("credentials_enable_service", false,
                "profile.password_manager_enabled", false, "profile.password_manager_leak_detection", false));
        options.setPageLoadTimeout(Duration.ofSeconds(30));
        return options;
    }
}
