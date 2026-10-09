package io.github.luismtueme.framework.screens;

import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import org.openqa.selenium.By;

/** Login screen of the Sauce Labs Mobile Sample App ("The App"). */
public class LoginScreen extends BaseScreen {

    private static final By USERNAME = accessId("test-Username");
    private static final By PASSWORD = accessId("test-Password");
    private static final By LOGIN = accessId("test-LOGIN");
    private static final By ERROR = accessId("test-Error message");

    public LoginScreen(AppiumDriver driver, Duration timeout) {
        super(driver, timeout);
    }

    @Override
    protected By readyIndicator() {
        return LOGIN;
    }

    public void login(String username, String password) {
        waitUntilLoaded();
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN);
    }

    /** Visible error text after a failed login attempt. */
    public String errorText() {
        return textOf(ERROR);
    }

    public boolean isDisplayed() {
        return isShown(LOGIN);
    }
}
