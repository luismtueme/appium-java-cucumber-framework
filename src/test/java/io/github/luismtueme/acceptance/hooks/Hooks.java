package io.github.luismtueme.acceptance.hooks;

import io.appium.java_client.AppiumDriver;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.github.luismtueme.acceptance.support.TestContext;
import java.nio.charset.StandardCharsets;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

/**
 * Cucumber lifecycle.
 *
 * <ul>
 *   <li>{@code @mobile}: a fresh Appium session per scenario; on failure, a screenshot and page source.
 *   <li>{@code @api}: no Appium session — use {@code context.api()} for HTTP calls.
 *   <li>Every scenario: cleanups run afterwards, pass or fail.
 * </ul>
 *
 * <p>Before hooks run in ascending {@code order}, after hooks in descending order.
 */
public class Hooks {

    private final TestContext context;

    public Hooks(TestContext context) {
        this.context = context;
    }

    @Before(order = 0)
    public void rememberScenario(Scenario scenario) {
        context.setScenario(scenario);
    }

    @Before(value = "@mobile", order = 10)
    public void startMobileSession() {
        context.startSession();
    }

    @After(order = 100)
    public void attachFailureEvidence(Scenario scenario) {
        if (!scenario.isFailed() || !context.hasSession()) {
            return;
        }
        AppiumDriver driver = context.driver();
        try {
            if (driver instanceof TakesScreenshot screenshot) {
                scenario.attach(screenshot.getScreenshotAs(OutputType.BYTES), "image/png", "Screenshot");
            }
            scenario.attach(driver.getPageSource().getBytes(StandardCharsets.UTF_8), "text/xml", "Page source");
            scenario.attach(context.config().platform().id(), "text/plain", "Platform");
        } catch (RuntimeException e) {
            scenario.log("Could not capture failure evidence: " + e.getMessage());
        }
    }

    @After(order = 50)
    public void runCleanups() {
        context.runCleanups();
    }

    @After(order = 10)
    public void quitSession() {
        context.quitSession();
    }
}
