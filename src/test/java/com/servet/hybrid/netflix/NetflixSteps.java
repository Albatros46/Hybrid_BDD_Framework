package com.servet.hybrid.netflix;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.servet.hybrid.framework.config.Env;
import com.servet.hybrid.framework.data.Unique;
import com.servet.hybrid.framework.mail.Mailbox;
import com.servet.hybrid.framework.mail.Mailbox.Mail;
import com.servet.hybrid.framework.web.Browser;
import com.servet.hybrid.netflix.NetflixWorld.Film;
import com.servet.hybrid.netflix.NetflixWorld.Member;
import com.servet.hybrid.netflix.pages.BrowsePage;
import com.servet.hybrid.netflix.pages.NetflixAuthPages;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

/** Netflix steps: sign-up and verification across browser and e-mail, films and "Meine Liste" across browser and API. */
public class NetflixSteps {

    private static final String UI = Env.get("netflix.ui.url");
    private static final String TOKEN_KEY = "nc_token";
    private static final String VERIFY_SUBJECT = "Netflix Clone - E-Mail-Adresse bestätigen";
    private static final Pattern LINK = Pattern.compile("(https?://\\S+/verify-email\\?token=[\\w-]+)");

    private final NetflixApi netflix;
    private final NetflixWorld world;
    private final Unique unique;
    private final Browser browser;
    private final Mailbox mailbox;

    public NetflixSteps(NetflixApi netflix, NetflixWorld world, Unique unique, Browser browser, Mailbox mailbox) {
        this.netflix = netflix;
        this.world = world;
        this.unique = unique;
        this.browser = browser;
        this.mailbox = mailbox;
    }

    private String adminToken() {
        if (world.adminToken() == null) {
            Response login = netflix.login(Env.get("netflix.admin.email"), Env.get("netflix.admin.password"));
            assertThat(login.statusCode()).as("admin login").isEqualTo(200);
            world.adminToken(login.path("token"));
        }
        return world.adminToken();
    }

    private Member newMember(String name) {
        Member member = new Member(name, unique.email(name), unique.password(name));
        world.member(member);
        return member;
    }

    /** The link from the verification e-mail; the e-mail is found by the member's unique address. */
    private String verificationLinkOf(String name) {
        Mail mail = mailbox.waitFor(VERIFY_SUBJECT, world.member(name).email());
        assertThat(mail.to()).isEqualTo(world.member(name).email());
        Matcher link = LINK.matcher(mail.text());
        assertThat(link.find()).as("verification link in the e-mail").isTrue();
        return link.group(1);
    }

    private long userId(String name) {
        List<Map<String, Object>> users = netflix.users(adminToken(), world.member(name).email()).path("content");
        return users.stream().filter(u -> world.member(name).email().equals(u.get("email")))
                .map(u -> ((Number) u.get("id")).longValue()).findFirst()
                .orElseThrow(() -> new AssertionError(name + " is not in the admin user list"));
    }

    // ---------------------------------------------------------------- arrange

    @Given("{string} has signed up for Netflix but not verified her e-mail")
    public void signedUpNotVerified(String name) {
        Member member = newMember(name);
        assertThat(netflix.signup(name, member.email(), member.password()).statusCode()).isEqualTo(200);
    }

    /** Sign-up through the API, verification with the token from the real e-mail, login through the API. */
    @Given("a verified Netflix member {string}")
    public void verifiedMember(String name) {
        signedUpNotVerified(name);
        String token = verificationLinkOf(name).replaceAll(".*token=", "");
        assertThat(netflix.verify(token).statusCode()).isEqualTo(200);
        logsInThroughApi(name);
    }

    @Given("the admin has published the film {string}")
    public void publishedFilm(String alias) {
        String title = unique.name(alias);
        assertThat(netflix.createFilm(adminToken(), title, true).statusCode()).isEqualTo(200);
        // the create endpoint answers only with a message, so the id is looked up by the unique title
        List<Map<String, Object>> films = netflix.adminFilms(adminToken(), title).path("content");
        long id = films.stream().filter(f -> title.equals(f.get("title")))
                .map(f -> ((Number) f.get("id")).longValue()).findFirst()
                .orElseThrow(() -> new AssertionError("created film " + title + " not found"));
        world.film(new Film(alias, title, id));
    }

    // ---------------------------------------------------------------- act (API)

    @When("{string} logs in to Netflix through the API")
    public void logsInThroughApi(String name) {
        Member member = world.member(name);
        Response login = netflix.login(member.email(), member.password());
        assertThat(login.statusCode()).as("login %s", name).isEqualTo(200);
        world.token(name, login.path("token"));
    }

    @When("{string} tries to log in to Netflix through the API")
    public void triesToLogIn(String name) {
        netflix.login(world.member(name).email(), world.member(name).password());
    }

    @When("the admin deactivates {string}")
    public void deactivates(String name) {
        Response answer = netflix.toggleStatus(adminToken(), String.valueOf(userId(name)));
        assertThat(answer.statusCode()).isEqualTo(200);
        assertThat((String) answer.path("message")).isEqualTo("User deactivated successfully!");
    }

    @When("the admin unpublishes the film {string}")
    public void unpublishes(String alias) {
        assertThat(netflix.publish(adminToken(), world.film(alias).id(), false).statusCode()).isEqualTo(200);
    }

    @When("{string} adds the film {string} to her list with her old token")
    public void addsWithOldToken(String name, String alias) {
        netflix.addToWatchlist(world.token(name), world.film(alias).id());
    }

    @When("the admin changes the status of the user with the id {string}")
    public void toggleInvalidId(String id) {
        netflix.toggleStatus(adminToken(), id);
    }

    // ---------------------------------------------------------------- act (browser, e-mail)

    @When("{string} signs up on the Netflix sign-up page")
    public void signsUpInBrowser(String name) {
        Member member = newMember(name);
        NetflixAuthPages pages = new NetflixAuthPages(browser.driver());
        pages.signUp(UI, name, member.email(), member.password());
        assertThat(pages.waitForTitle("Fast geschafft!")).isEqualTo("Fast geschafft!");
    }

    @Then("{string} receives a verification e-mail with a link to the Netflix app")
    public void receivesVerificationMail(String name) {
        String link = verificationLinkOf(name);
        assertThat(link).startsWith(Env.get("netflix.mail.link.base"));
        world.verificationLink(link);
    }

    @When("she opens the verification link in the browser")
    public void opensLink() {
        // the link points to the frontend address the API was configured with; open the same path on the tested UI
        browser.driver().get(UI + world.verificationLink().replaceAll("^https?://[^/]+", ""));
    }

    @When("{string} logs in on the Netflix login page")
    public void logsInInBrowser(String name) {
        new NetflixAuthPages(browser.driver()).logIn(UI, world.member(name).email(), world.member(name).password());
    }

    @When("{string} searches for the film {string} in the browser")
    public void searches(String name, String alias) {
        browser.openWithSession(UI, TOKEN_KEY, world.token(name), "/browse");
        new BrowsePage(browser.driver()).search(world.film(alias).title());
    }

    @When("she adds {string} to her list in the browser")
    public void addsInBrowser(String alias) {
        new BrowsePage(browser.driver()).addToMyList(world.film(alias).title());
    }

    @When("{string} opens her list in the browser")
    public void opensList(String name) {
        browser.openWithSession(UI, TOKEN_KEY, world.token(name), "/watchlist");
    }

    // ---------------------------------------------------------------- assert (browser)

    @Then("the page confirms {string}")
    public void pageConfirms(String text) {
        assertThat(new NetflixAuthPages(browser.driver()).success()).startsWith(text);
    }

    @Then("the page shows the error {string}")
    public void loginShows(String message) {
        assertThat(new NetflixAuthPages(browser.driver()).error()).isEqualTo(message);
    }

    @Then("the login page offers to send the verification e-mail again")
    public void resendOffered() {
        assertThat(new NetflixAuthPages(browser.driver()).resendOffered()).isTrue();
    }

    @Then("the browser shows the film overview")
    public void showsBrowse() {
        new NetflixAuthPages(browser.driver()).waitForPath("/browse");
    }

    @Then("the search result shows the film {string}")
    public void searchShows(String alias) {
        assertThat(new BrowsePage(browser.driver()).titles()).contains(world.film(alias).title());
    }

    @Then("the search in the browser finds nothing for {string}")
    public void searchFindsNothing(String alias) {
        BrowsePage page = new BrowsePage(browser.driver());
        browser.driver().navigate().refresh();
        page.search(world.film(alias).title());
        assertThat(page.nothingFound()).isTrue();
    }

    @Then("her list in the browser shows {string}")
    public void listShows(String alias) {
        assertThat(new BrowsePage(browser.driver()).myList()).containsExactly(world.film(alias).title());
    }

    // ---------------------------------------------------------------- assert (API)

    @Then("the API has {string} on the list of {string}")
    public void apiList(String alias, String name) {
        List<Map<String, Object>> films = netflix.watchlist(world.token(name)).path("content");
        assertThat(films).extracting(f -> f.get("title")).containsExactly(world.film(alias).title());
    }

    @Then("the API answers 404 to {string} for the film {string}")
    public void apiFilm404(String name, String alias) {
        assertThat(netflix.film(world.token(name), world.film(alias).id()).statusCode()).isEqualTo(404);
    }

    @Then("the old token of {string} is rejected with status {int}")
    public void oldTokenRejected(String name, int status) {
        assertThat(netflix.currentUser(world.token(name)).statusCode()).as("current-user with the old token")
                .isEqualTo(status);
    }
}
