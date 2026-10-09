package io.github.luismtueme.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.luismtueme.acceptance.support.Eventually;
import io.github.luismtueme.acceptance.support.TestContext;
import io.github.luismtueme.framework.config.Credentials;

public class LoginSteps {

    private final TestContext context;

    public LoginSteps(TestContext context) {
        this.context = context;
    }

    @Given("I am on the login screen")
    public void iAmOnTheLoginScreen() {
        context.loginScreen().waitUntilLoaded();
    }

    @When("I log in with the configured credentials")
    public void iLogInWithTheConfiguredCredentials() {
        Credentials credentials = context.config().requireCredentials();
        context.loginScreen().login(credentials.username(), credentials.password());
    }

    @When("I log in with username {string} and password {string}")
    public void iLogInWithUsernameAndPassword(String username, String password) {
        context.loginScreen().login(username, password);
    }

    @Then("I see the products catalog")
    public void iSeeTheProductsCatalog() {
        Eventually.assertThat(
                () -> assertThat(context.catalogScreen().isDisplayed()).isTrue());
        assertThat(context.catalogScreen().productsHeading()).containsIgnoringCase("PRODUCTS");
    }

    @Then("I see a login error containing {string}")
    public void iSeeALoginErrorContaining(String fragment) {
        Eventually.assertThat(
                () -> assertThat(context.loginScreen().errorText()).containsIgnoringCase(fragment));
    }
}
