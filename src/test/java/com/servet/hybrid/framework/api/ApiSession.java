package com.servet.hybrid.framework.api;

import static io.restassured.RestAssured.given;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * REST Assured for one scenario (PicoContainer): the request start for any base URL and the
 * last response, which the generic Then steps check and the hooks attach on failure.
 */
public class ApiSession {

    private Response last;

    public RequestSpecification request(String baseUrl) {
        return given().baseUri(baseUrl).accept("application/json").contentType(ContentType.JSON);
    }

    public RequestSpecification request(String baseUrl, String bearerToken) {
        return request(baseUrl).auth().oauth2(bearerToken);
    }

    public Response remember(Response response) {
        last = response;
        return response;
    }

    public Response last() {
        if (last == null) {
            throw new IllegalStateException("No API call in this scenario yet");
        }
        return last;
    }

    public boolean hasResponse() {
        return last != null;
    }
}
