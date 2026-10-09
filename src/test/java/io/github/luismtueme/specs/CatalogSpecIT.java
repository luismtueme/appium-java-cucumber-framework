package io.github.luismtueme.specs;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.luismtueme.acceptance.support.Eventually;
import io.github.luismtueme.framework.config.Credentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("mobile")
@Tag("Regression")
@DisplayName("Catalog (JUnit specs)")
class CatalogSpecIT extends MobileSession {

    @Test
    @DisplayName("shows the cart control after login")
    void showsCartAfterLogin() {
        Credentials credentials = config.requireCredentials();
        loginScreen.login(credentials.username(), credentials.password());

        Eventually.assertThat(() -> assertThat(catalogScreen.isDisplayed()).isTrue());
        assertThat(catalogScreen.cartIsShown()).isTrue();
    }
}
