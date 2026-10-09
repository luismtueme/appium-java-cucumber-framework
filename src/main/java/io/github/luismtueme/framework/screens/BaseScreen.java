package io.github.luismtueme.framework.screens;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Shared behavior for screen objects.
 *
 * <p>Screen objects expose user actions ({@code login(...)}) and reads of what the user sees
 * ({@code productsHeading()}). They wait for elements before using them, but never assert: a failure points at the
 * step or spec that made the claim. ArchitectureTest enforces both rules.
 *
 * <p>Prefer accessibility ids that exist on both Android and iOS for the demo sample app.
 */
public abstract class BaseScreen {

    protected final AppiumDriver driver;
    protected final WebDriverWait wait;

    protected BaseScreen(AppiumDriver driver, Duration timeout) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, timeout);
    }

    /** The element that shows the screen has loaded. */
    protected abstract By readyIndicator();

    public void waitUntilLoaded() {
        visible(readyIndicator());
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement field = visible(locator);
        field.clear();
        field.sendKeys(text);
    }

    /** Current text of an element, waiting until it is visible. */
    protected String textOf(By locator) {
        return visible(locator).getText();
    }

    protected boolean isShown(By locator) {
        return !driver.findElements(locator).isEmpty()
                && driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    /** Accessibility id shared by the Sauce Labs sample app on Android and iOS. */
    protected static By accessId(String id) {
        return AppiumBy.accessibilityId(id);
    }
}
