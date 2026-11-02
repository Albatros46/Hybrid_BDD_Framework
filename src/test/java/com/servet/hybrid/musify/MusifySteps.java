package com.servet.hybrid.musify;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import com.servet.hybrid.framework.config.Env;
import com.servet.hybrid.framework.data.Unique;
import com.servet.hybrid.framework.web.Browser;
import com.servet.hybrid.musify.MusifyWorld.User;
import com.servet.hybrid.musify.pages.LibraryPage;
import com.servet.hybrid.musify.pages.MusifyLoginPage;
import com.servet.hybrid.musify.pages.PlaylistPage;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

/** Musify steps. Given = arrange through the API, When = act in the browser, Then = check where it matters. */
public class MusifySteps {

    private static final String UI = Env.get("musify.ui.url");

    private final MusifyApi musify;
    private final MusifyWorld world;
    private final Unique unique;
    private final Browser browser;

    public MusifySteps(MusifyApi musify, MusifyWorld world, Unique unique, Browser browser) {
        this.musify = musify;
        this.world = world;
        this.unique = unique;
        this.browser = browser;
    }

    // ---------------------------------------------------------------- arrange (API)

    @Given("a Musify listener {string}")
    public void listener(String name) {
        String email = unique.email(name);
        String password = unique.password(name);
        assertThat(musify.register(name, email, password).statusCode()).as("register %s", name).isEqualTo(201);
        Response login = musify.login(email, password);
        assertThat(login.statusCode()).as("login %s", name).isEqualTo(200);
        world.user(new User(((Number) login.path("id")).longValue(), name, email, password, login.path("accessToken"),
                login.path("refreshToken")));
    }

    @Given("{string} has a {word} playlist {string} with the songs:")
    public void playlistWithSongs(String user, String visibility, String name, List<String> titles) {
        String token = world.user(user).accessToken();
        Response created = musify.createPlaylist(token, name, "public".equals(visibility));
        assertThat(created.statusCode()).isEqualTo(201);
        long playlistId = ((Number) created.path("id")).longValue();
        world.playlist(name, playlistId);
        for (String title : titles) {
            Response song = musify.uploadSong(token, title);
            assertThat(song.statusCode()).as("upload %s", title).isEqualTo(201);
            long songId = ((Number) song.path("id")).longValue();
            world.song(title, songId);
            assertThat(musify.addSong(token, playlistId, songId).statusCode()).isEqualTo(200);
        }
    }

    // ---------------------------------------------------------------- act (browser)

    @When("{string} opens her library in the browser")
    public void opensLibrary(String user) {
        openAs(user, "/library");
    }

    @When("{string} opens the playlist {string} in the browser")
    public void opensPlaylist(String user, String playlist) {
        openAs(user, "/playlists/" + world.playlist(playlist));
    }

    /** Logged in through the API; the browser only gets the session (refresh token) and opens the page. */
    private void openAs(String user, String path) {
        browser.openWithSession(UI, "musify.refreshToken", world.user(user).refreshToken(), path);
    }

    @When("{string} creates the {word} playlist {string} in her library")
    public void createsInLibrary(String user, String visibility, String name) {
        openAs(user, "/library");
        LibraryPage library = new LibraryPage(browser.driver());
        library.create(name, "public".equals(visibility));
        assertThat(library.subtitleOf(name)).startsWith("public".equals(visibility) ? "Public" : "Private");
    }

    @When("she removes {string} from the playlist")
    public void removesSong(String title) {
        new PlaylistPage(browser.driver()).loaded().remove(title);
    }

    @When("she makes the playlist public")
    public void makesPublic() {
        new PlaylistPage(browser.driver()).loaded().makePublic();
    }

    @When("she deletes the playlist and confirms")
    public void deletes() {
        new PlaylistPage(browser.driver()).loaded().delete();
    }

    @When("{string} logs in on the Musify login page with a wrong password")
    public void wrongPassword(String user) {
        new MusifyLoginPage(browser.driver()).open(UI).logIn(world.user(user).email(), "wrong-" + unique.id());
    }

    // ---------------------------------------------------------------- assert (browser)

    @Then("the library shows {string} as {string}")
    public void libraryShows(String playlist, String subtitle) {
        assertThat(new LibraryPage(browser.driver()).subtitleOf(playlist)).isEqualTo(subtitle);
    }

    @Then("the playlist page shows {string}, {string} and {string}")
    public void playlistPageShows(String visibility, String name, String ownerLine) {
        PlaylistPage page = new PlaylistPage(browser.driver()).loaded();
        assertThat(page.visibility()).isEqualTo(visibility);
        assertThat(page.heading()).isEqualTo(name);
        assertThat(page.ownerLine()).isEqualTo(ownerLine);
    }

    @Then("the songs are listed in this order:")
    public void songsInOrder(List<String> titles) {
        assertThat(new PlaylistPage(browser.driver()).loaded().songTitles()).containsExactlyElementsOf(titles);
    }

    @Then("the browser shows the error that the playlist {string} was not found")
    public void browserNotFound(String playlist) {
        assertThat(new PlaylistPage(browser.driver()).error())
                .isEqualTo("Playlist " + world.playlist(playlist) + " not found");
    }

    @Then("the Musify login page shows {string}")
    public void loginError(String message) {
        assertThat(new MusifyLoginPage(browser.driver()).error()).isEqualTo(message);
    }

    // ---------------------------------------------------------------- assert (API)

    @Then("the API lists {string} among the public playlists, owned by {string}")
    public void apiListsPublic(String name, String owner) {
        Response list = musify.publicPlaylists(world.user(owner).accessToken());
        List<Map<String, Object>> content = list.path("content");
        long ownerId = world.user(owner).id();
        // the name alone is not unique: earlier runs left playlists with the same name in the database
        assertThat(content).filteredOn(p -> name.equals(p.get("name"))
                && ownerId == ((Number) p.get("appUserId")).longValue())
                .singleElement()
                .satisfies(p -> assertThat(p.get("appUserName")).isEqualTo(owner));
    }

    @Then("the API returns the playlist {string} to {string} with the songs:")
    public void apiReturnsSongs(String playlist, String user, List<Map<String, String>> expected) {
        Response answer = musify.playlist(world.user(user).accessToken(), world.playlist(playlist));
        assertThat(answer.statusCode()).isEqualTo(200);
        List<Map<String, Object>> songs = answer.path("songs");
        assertThat(songs).hasSize(expected.size());
        for (int i = 0; i < expected.size(); i++) {
            assertThat(String.valueOf(songs.get(i).get("position"))).isEqualTo(expected.get(i).get("position"));
            assertThat(songs.get(i).get("title")).isEqualTo(expected.get(i).get("title"));
        }
    }

    @Then("{string} can read the playlist {string} through the API")
    public void canRead(String user, String playlist) {
        assertThat(musify.playlist(world.user(user).accessToken(), world.playlist(playlist)).statusCode())
                .isEqualTo(200);
    }

    @Then("the API answers 404 to {string} for the playlist {string}")
    public void apiNotFound(String user, String playlist) {
        assertThat(musify.playlist(world.user(user).accessToken(), world.playlist(playlist)).statusCode())
                .isEqualTo(404);
    }
}
