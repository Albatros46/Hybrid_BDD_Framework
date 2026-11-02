package com.servet.hybrid.framework.hooks;

import java.util.concurrent.atomic.AtomicInteger;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import com.servet.hybrid.framework.api.ApiSession;
import com.servet.hybrid.framework.api.Masking;
import com.servet.hybrid.framework.web.Browser;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;

/** The same evidence for every application: screenshot and last API response after a failed step. */
public class FrameworkHooks {

    private static final AtomicInteger SCENARIOS = new AtomicInteger();

    private final Browser browser;
    private final ApiSession api;

    public FrameworkHooks(Browser browser, ApiSession api) {
        this.browser = browser;
        this.api = api;
    }

    @Before
    public void count() {
        SCENARIOS.incrementAndGet();
    }

    @AfterStep
    public void evidenceOnFailure(Scenario scenario) {
        if (!scenario.isFailed()) {
            return;
        }
        if (browser.isStarted()) {
            scenario.attach(((TakesScreenshot) browser.driver()).getScreenshotAs(OutputType.BYTES), "image/png",
                    "Screenshot after the failed step");
        }
        if (api.hasResponse()) {
            scenario.attach(api.last().getStatusCode() + " " + Masking.mask(api.last().asString()), "text/plain",
                    "Last API response (secrets masked)");
        }
    }

    @After
    public void closeBrowser() {
        browser.quit();
    }

    @AfterAll
    public static void summary() {
        System.out.printf("%d scenarios, %d of them needed a browser%n", SCENARIOS.get(), Browser.STARTED.get());
    }
}
