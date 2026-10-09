package io.github.luismtueme.specs;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.luismtueme.acceptance.support.Eventually;
import io.github.luismtueme.framework.config.Credentials;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("mobile")
@Tag("Smoke")
@DisplayName("Login (JUnit specs)")
class LoginSpecIT extends MobileSession {

    @Test
    @DisplayName("logs in with the configured credentials and shows the catalog")
    void logsInWithConfiguredCredentials() {
        Credentials credentials = config.requireCredentials();
        loginScreen.login(credentials.username(), credentials.password());

        Eventually.assertThat(() -> assertThat(catalogScreen.isDisplayed()).isTrue());
        assertThat(catalogScreen.productsHeading()).containsIgnoringCase("PRODUCTS");
    }

    @Test
    @Tag("Regression")
    @DisplayName("shows an error for a locked-out user")
    void showsErrorForLockedOutUser() {
        loginScreen.login("locked_out_user", "secret_sauce");

        Eventually.assertThat(() -> assertThat(loginScreen.errorText()).containsIgnoringCase("locked out"));
    }
}
