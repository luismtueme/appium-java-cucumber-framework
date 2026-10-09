package io.github.luismtueme.acceptance.steps;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.luismtueme.acceptance.support.TestContext;
import io.restassured.response.Response;

public class ApiSteps {

    private final TestContext context;

    public ApiSteps(TestContext context) {
        this.context = context;
    }

    @When("I GET {string}")
    public void iGet(String path) {
        context.setLastResponse(context.api().get(path));
    }

    @Then("the API response status is {int}")
    public void theApiResponseStatusIs(int status) {
        assertThat(context.lastResponse().statusCode()).isEqualTo(status);
    }

    @Then("the API response JSON field {string} equals {int}")
    public void theApiResponseJsonFieldEquals(String field, int value) {
        Response response = context.lastResponse();
        assertThat(response.jsonPath().getInt(field)).isEqualTo(value);
    }
}
