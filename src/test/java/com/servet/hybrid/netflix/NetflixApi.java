package com.servet.hybrid.netflix;

import java.util.List;
import java.util.Map;

import com.servet.hybrid.framework.api.ApiSession;
import com.servet.hybrid.framework.config.Env;

import io.restassured.response.Response;

/** The endpoints of the Netflix Clone API that the scenarios use. Every answer is remembered in the ApiSession. */
public class NetflixApi {

    public static final String URL = Env.get("netflix.api.url");

    private final ApiSession api;

    public NetflixApi(ApiSession api) {
        this.api = api;
    }

    public Response signup(String fullName, String email, String password) {
        return api.remember(api.request(URL)
                .body(Map.of("fullName", fullName, "email", email, "password", password))
                .post("/api/auth/signup"));
    }

    public Response verify(String token) {
        return api.remember(api.request(URL).queryParam("token", token).get("/api/auth/verify-email"));
    }

    public Response login(String email, String password) {
        return api.remember(api.request(URL).body(Map.of("email", email, "password", password))
                .post("/api/auth/login"));
    }

    public Response currentUser(String token) {
        return api.remember(api.request(URL, token).get("/api/auth/current-user"));
    }

    public Response createFilm(String adminToken, String title, boolean published) {
        return api.remember(api.request(URL, adminToken).body(Map.of(
                "title", title,
                "description", "A film created by the hybrid BDD tests.",
                "year", 2026,
                "rating", "12",
                "duration", 95,
                "published", published,
                "categories", List.of("Drama"))).post("/api/videos/admin"));
    }

    public Response adminFilms(String adminToken, String search) {
        return api.remember(api.request(URL, adminToken).queryParam("search", search).queryParam("size", 50)
                .get("/api/videos/admin"));
    }

    public Response publish(String adminToken, long filmId, boolean published) {
        return api.remember(api.request(URL, adminToken).queryParam("value", published)
                .patch("/api/videos/admin/{id}/publish", filmId));
    }

    public Response film(String token, long filmId) {
        return api.remember(api.request(URL, token).get("/api/videos/{id}", filmId));
    }

    public Response watchlist(String token) {
        return api.remember(api.request(URL, token).queryParam("size", 100).get("/api/videos/watchlist"));
    }

    public Response addToWatchlist(String token, long filmId) {
        return api.remember(api.request(URL, token).post("/api/videos/watchlist/{id}", filmId));
    }

    public Response users(String adminToken, String search) {
        return api.remember(api.request(URL, adminToken).queryParam("search", search).get("/api/users"));
    }

    public Response toggleStatus(String adminToken, String userId) {
        return api.remember(api.request(URL, adminToken).put("/api/users/{id}/toggle-status", userId));
    }
}
