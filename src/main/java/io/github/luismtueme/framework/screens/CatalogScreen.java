package io.github.luismtueme.framework.screens;

import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/** Product catalog shown after a successful login. */
public class CatalogScreen extends BaseScreen {

    private static final By PRODUCTS = accessId("test-PRODUCTS");
    private static final By CART = accessId("test-Cart");
    private static final String PRODUCTS_TEST_ID = "test-PRODUCTS";

    public CatalogScreen(AppiumDriver driver, Duration timeout) {
        super(driver, timeout);
    }

    @Override
    protected By readyIndicator() {
        return PRODUCTS;
    }

    /**
     * Visible catalog title when available.
     *
     * <p>On XCUITest, React Native {@code testID} maps to the {@code name} attribute (identifier), not the
     * painted title. {@code getText()} on that container often returns concatenated child product names.
     * Prefer {@code label}, then a {@code PRODUCTS} fragment in {@code getText()}, and never treat the
     * test id itself as the heading.
     */
    public String productsHeading() {
        WebElement el = visible(PRODUCTS);
        String label = el.getAttribute("label");
        if (label != null && !label.isBlank() && !PRODUCTS_TEST_ID.equals(label)) {
            return label;
        }
        String text = el.getText();
        if (text != null && text.toUpperCase().contains("PRODUCTS")) {
            return "PRODUCTS";
        }
        String contentDesc = el.getAttribute("contentDescription");
        if (contentDesc != null && !contentDesc.isBlank() && !PRODUCTS_TEST_ID.equals(contentDesc)) {
            return contentDesc;
        }
        return text == null ? "" : text;
    }

    public boolean isDisplayed() {
        return isShown(PRODUCTS);
    }

    public boolean cartIsShown() {
        return isShown(CART);
    }
}
