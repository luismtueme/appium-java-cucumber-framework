package io.github.luismtueme.framework.screens;

import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import org.openqa.selenium.By;

/** Product catalog shown after a successful login. */
public class CatalogScreen extends BaseScreen {

    private static final By PRODUCTS = accessId("test-PRODUCTS");
    private static final By CART = accessId("test-Cart");

    public CatalogScreen(AppiumDriver driver, Duration timeout) {
        super(driver, timeout);
    }

    @Override
    protected By readyIndicator() {
        return PRODUCTS;
    }

    /** Heading text that confirms the catalog is open ("PRODUCTS"). */
    public String productsHeading() {
        return textOf(PRODUCTS);
    }

    public boolean isDisplayed() {
        return isShown(PRODUCTS);
    }

    public boolean cartIsShown() {
        return isShown(CART);
    }
}
