package com.servet.hybrid.framework.mail;

import static io.restassured.RestAssured.given;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.servet.hybrid.framework.config.Env;

import io.restassured.response.Response;

/**
 * Reads the e-mails an application sent, from Mailpit's REST API (the SMTP catcher the apps use
 * in development and in CI). E-mails arrive asynchronously, so it polls for up to 15 seconds.
 */
public class Mailbox {

    public record Mail(String id, String subject, String to, String text) {
    }

    private final String baseUrl = Env.get("mailpit.url");

    /** The first e-mail whose subject contains the text and whose body contains the marker (e.g. a unique address). */
    public Mail waitFor(String subjectContains, String bodyContains) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(15));
        while (true) {
            Response search = given().baseUri(baseUrl).queryParam("query", "\"" + bodyContains + "\"")
                    .get("/api/v1/search");
            List<Map<String, Object>> messages = search.path("messages");
            if (messages != null) {
                for (Map<String, Object> message : messages) {
                    String subject = String.valueOf(message.get("Subject"));
                    if (subject.contains(subjectContains)) {
                        return read(String.valueOf(message.get("ID")));
                    }
                }
            }
            if (Instant.now().isAfter(deadline)) {
                throw new AssertionError("No e-mail with subject containing '" + subjectContains + "' and text '"
                        + bodyContains + "' arrived within 15 s");
            }
            sleep();
        }
    }

    private Mail read(String id) {
        Response message = given().baseUri(baseUrl).get("/api/v1/message/{id}", id);
        List<Map<String, Object>> to = message.path("To");
        return new Mail(id, message.path("Subject"), to.isEmpty() ? "" : String.valueOf(to.getFirst().get("Address")),
                message.path("Text"));
    }

    private static void sleep() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
