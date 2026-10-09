package io.github.luismtueme.specs;

import io.appium.java_client.AppiumDriver;
import io.github.luismtueme.framework.config.Config;
import io.github.luismtueme.framework.driver.DriverFactory;
import io.github.luismtueme.framework.screens.CatalogScreen;
import io.github.luismtueme.framework.screens.LoginScreen;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Shared Appium lifecycle for plain JUnit specs. Specs and Cucumber steps use the same screen objects and
 * {@link Config}.
 */
public abstract class MobileSession {

    protected final Config config = Config.get();
    protected AppiumDriver driver;
    protected LoginScreen loginScreen;
    protected CatalogScreen catalogScreen;

    @BeforeEach
    void startSession() {
        driver = DriverFactory.create(config);
        loginScreen = new LoginScreen(driver, config.waitTimeout());
        catalogScreen = new CatalogScreen(driver, config.waitTimeout());
    }

    @AfterEach
    void quitSession() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
