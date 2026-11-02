package com.servet.hybrid.musify;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import com.servet.hybrid.framework.api.ApiSession;
import com.servet.hybrid.framework.config.Env;

import io.restassured.response.Response;

/** The endpoints of the Musify API that the scenarios use. Every answer is remembered in the ApiSession. */
public class MusifyApi {

    public static final String URL = Env.get("musify.api.url");

    private final ApiSession api;

    public MusifyApi(ApiSession api) {
        this.api = api;
    }

    public Response register(String name, String email, String password) {
        return api.remember(api.request(URL).body(Map.of("name", name, "email", email, "password", password))
                .post("/api/auth/register"));
    }

    public Response login(String email, String password) {
        return api.remember(api.request(URL).body(Map.of("email", email, "password", password))
                .post("/api/auth/login"));
    }

    public Response uploadSong(String token, String title) {
        return api.remember(api.request(URL, token).contentType("multipart/form-data")
                .multiPart("title", title)
                .multiPart("artist", "Hybrid Artist")
                .multiPart("album", "Hybrid Album")
                .multiPart("file", "song.wav", silentWav(), "audio/wav")
                .post("/api/songs"));
    }

    public Response createPlaylist(String token, String name, boolean isPublic) {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("isPublic", isPublic);
        return api.remember(api.request(URL, token).body(body).post("/api/playlists"));
    }

    public Response addSong(String token, long playlistId, long songId) {
        return api.remember(api.request(URL, token).post("/api/playlists/{id}/songs/{songId}", playlistId, songId));
    }

    public Response playlist(String token, long playlistId) {
        return api.remember(api.request(URL, token).get("/api/playlists/{id}", playlistId));
    }

    public Response publicPlaylists(String token) {
        return api.remember(api.request(URL, token).queryParam("size", 100).get("/api/playlists/public"));
    }

    /** 0.1 s of silence: a valid WAV, enough for the API to accept it as a song. */
    private static byte[] silentWav() {
        int samples = 800;
        ByteBuffer b = ByteBuffer.allocate(44 + samples * 2).order(ByteOrder.LITTLE_ENDIAN);
        b.put("RIFF".getBytes()).putInt(36 + samples * 2).put("WAVE".getBytes()).put("fmt ".getBytes())
                .putInt(16).putShort((short) 1).putShort((short) 1).putInt(8000).putInt(16000)
                .putShort((short) 2).putShort((short) 16).put("data".getBytes()).putInt(samples * 2);
        return b.array();
    }
}
