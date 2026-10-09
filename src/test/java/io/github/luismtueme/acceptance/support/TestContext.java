package io.github.luismtueme.acceptance.support;

import io.appium.java_client.AppiumDriver;
import io.cucumber.java.Scenario;
import io.github.luismtueme.framework.api.ApiClient;
import io.github.luismtueme.framework.config.Config;
import io.github.luismtueme.framework.driver.DriverFactory;
import io.github.luismtueme.framework.screens.CatalogScreen;
import io.github.luismtueme.framework.screens.LoginScreen;
import io.restassured.response.Response;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Everything one scenario shares between its steps and hooks: the Appium session, screen objects, API client, the last
 * API response and the cleanups to run afterwards.
 *
 * <p>PicoContainer creates a new instance per scenario and injects it into every step class and {@code Hooks} through
 * their constructors, so parallel scenarios never share state.
 */
public class TestContext {

    private final Config config = Config.get();
    private final Deque<Cleanup> cleanups = new ArrayDeque<>();
    private AppiumDriver driver;
    private LoginScreen loginScreen;
    private CatalogScreen catalogScreen;
    private ApiClient api;
    private Response lastResponse;
    private Scenario scenario;

    public Config config() {
        return config;
    }

    /** Set by a hook before every scenario, so steps and clients can attach evidence to the report. */
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    /** Adds text to the scenario in the Cucumber report (no-op outside a scenario). */
    public void attach(String content, String mediaType, String name) {
        if (scenario != null) {
            scenario.attach(content, mediaType, name);
        }
    }

    public void attach(byte[] content, String mediaType, String name) {
        if (scenario != null) {
            scenario.attach(content, mediaType, name);
        }
    }

    // --- Mobile session --------------------------------------------------------------------------------------------

    /** Started by the {@code @mobile} hook. */
    public void startSession() {
        driver = DriverFactory.create(config);
    }

    public boolean hasSession() {
        return driver != null;
    }

    public AppiumDriver driver() {
        if (driver == null) {
            throw new IllegalStateException("No Appium session in this scenario. Tag the feature or scenario @mobile.");
        }
        return driver;
    }

    public void quitSession() {
        if (driver != null) {
            driver.quit();
        }
        driver = null;
        loginScreen = null;
        catalogScreen = null;
    }

    public LoginScreen loginScreen() {
        if (loginScreen == null) {
            loginScreen = new LoginScreen(driver(), config.waitTimeout());
        }
        return loginScreen;
    }

    public CatalogScreen catalogScreen() {
        if (catalogScreen == null) {
            catalogScreen = new CatalogScreen(driver(), config.waitTimeout());
        }
        return catalogScreen;
    }

    // --- API -------------------------------------------------------------------------------------------------------

    public ApiClient api() {
        if (api == null) {
            api = new ApiClient(config.apiBaseUrl())
                    .recordingTo((name, json) -> attach(json, "application/json", name));
        }
        return api;
    }

    public Response lastResponse() {
        if (lastResponse == null) {
            throw new IllegalStateException("No API request has been sent in this scenario");
        }
        return lastResponse;
    }

    public void setLastResponse(Response response) {
        lastResponse = response;
    }

    // --- Cleanup ---------------------------------------------------------------------------------------------------

    /** Runs after the scenario, pass or fail, in reverse order of registration. */
    public void addCleanup(String description, Runnable action) {
        cleanups.push(new Cleanup(description, action));
    }

    /** Runs every cleanup, even when one fails, then reports all failures together. */
    public void runCleanups() {
        List<String> failures = new ArrayList<>();
        while (!cleanups.isEmpty()) {
            Cleanup cleanup = cleanups.pop();
            try {
                cleanup.action().run();
            } catch (RuntimeException e) {
                failures.add(cleanup.description() + ": " + e.getMessage());
            }
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("Cleanup failed: " + String.join("; ", failures));
        }
    }

    private record Cleanup(String description, Runnable action) {}
}
