package com.servet.hybrid.netflix;

import java.util.HashMap;
import java.util.Map;

/** What a Netflix scenario created, by the names the feature file uses. One instance per scenario. */
public class NetflixWorld {

    public record Member(String name, String email, String password) {
        @Override
        public String toString() {
            return "Member[" + name + "]";
        }
    }

    public record Film(String alias, String title, long id) {
    }

    private final Map<String, Member> members = new HashMap<>();
    private final Map<String, String> tokens = new HashMap<>();
    private final Map<String, Film> films = new HashMap<>();
    private String adminToken;
    private String verificationLink;

    public void member(Member member) {
        members.put(member.name(), member);
    }

    public Member member(String name) {
        return found(members.get(name), "member", name);
    }

    public void token(String name, String token) {
        tokens.put(name, token);
    }

    public String token(String name) {
        return found(tokens.get(name), "token of", name);
    }

    public String adminToken() {
        return adminToken;
    }

    public void adminToken(String token) {
        adminToken = token;
    }

    public void film(Film film) {
        films.put(film.alias(), film);
    }

    public Film film(String alias) {
        return found(films.get(alias), "film", alias);
    }

    public void verificationLink(String link) {
        verificationLink = link;
    }

    public String verificationLink() {
        return found(verificationLink, "verification link", "of this scenario");
    }

    private static <T> T found(T value, String kind, String name) {
        if (value == null) {
            throw new IllegalStateException("Unknown " + kind + " \"" + name + "\" in this scenario");
        }
        return value;
    }
}
