package io.github.luismtueme.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.github.luismtueme.acceptance.support.TestContext;

public class CatalogSteps {

    private final TestContext context;

    public CatalogSteps(TestContext context) {
        this.context = context;
    }

    @Then("the cart control is visible")
    public void theCartControlIsVisible() {
        assertThat(context.catalogScreen().cartIsShown()).isTrue();
    }
}
