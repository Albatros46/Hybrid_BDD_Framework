package com.servet.hybrid.framework.steps;

import static org.assertj.core.api.Assertions.assertThat;

import com.servet.hybrid.framework.api.ApiSession;

import io.cucumber.java.en.Then;

/** Generic checks on the last API response, for every application. */
public class ResponseSteps {

    private final ApiSession api;

    public ResponseSteps(ApiSession api) {
        this.api = api;
    }

    @Then("the response status is {int}")
    public void status(int expected) {
        assertThat(api.last().statusCode()).as("HTTP status").isEqualTo(expected);
    }

    /** Apps name the field differently: Musify uses "message", Netflix Clone "error". */
    @Then("the error message is {string}")
    public void message(String expected) {
        String message = api.last().path("message");
        assertThat(message != null ? message : (String) api.last().path("error")).isEqualTo(expected);
    }
}
