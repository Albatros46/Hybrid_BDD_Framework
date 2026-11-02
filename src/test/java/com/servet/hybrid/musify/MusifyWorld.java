package com.servet.hybrid.musify;

import java.util.HashMap;
import java.util.Map;

/** What a Musify scenario created, by the names the feature file uses. One instance per scenario. */
public class MusifyWorld {

    public record User(long id, String name, String email, String password, String accessToken, String refreshToken) {
        @Override
        public String toString() {
            return "User[" + name + "]"; // never the password or the tokens
        }
    }

    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Long> playlists = new HashMap<>();
    private final Map<String, Long> songs = new HashMap<>();

    public void user(User user) {
        users.put(user.name(), user);
    }

    public User user(String name) {
        return found(users.get(name), "user", name);
    }

    public void playlist(String name, long id) {
        playlists.put(name, id);
    }

    public long playlist(String name) {
        return found(playlists.get(name), "playlist", name);
    }

    public void song(String title, long id) {
        songs.put(title, id);
    }

    public long song(String title) {
        return found(songs.get(title), "song", title);
    }

    private static <T> T found(T value, String kind, String name) {
        if (value == null) {
            throw new IllegalStateException("Unknown " + kind + " \"" + name + "\" in this scenario");
        }
        return value;
    }
}
