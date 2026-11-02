package com.servet.hybrid.netflix.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.servet.hybrid.framework.web.BasePage;

/** Sign-up, verification and login: the pages a new member sees before the first film. */
public class NetflixAuthPages extends BasePage {

    private static final By TITLE = By.cssSelector("h1");

    public NetflixAuthPages(WebDriver driver) {
        super(driver);
    }

    public void signUp(String appUrl, String fullName, String email, String password) {
        driver.get(appUrl + "/signup");
        type(By.cssSelector("input[placeholder='Vollständiger Name']"), fullName);
        type(By.cssSelector("input[placeholder='E-Mail-Adresse']"), email);
        type(By.cssSelector("input[placeholder^='Passwort (mind']"), password);
        type(By.cssSelector("input[placeholder='Passwort bestätigen']"), password);
        click(button("Konto erstellen"));
    }

    public void logIn(String appUrl, String email, String password) {
        driver.get(appUrl + "/login");
        type(By.cssSelector("input[placeholder='E-Mail-Adresse']"), email);
        type(By.cssSelector("input[placeholder='Passwort']"), password);
        click(button("Anmelden"));
    }

    public String title() {
        return text(TITLE);
    }

    /** Waits for the page that follows a form submit; on a red message the wait ends early with that message. */
    public String waitForTitle(String expected) {
        wait.until(d -> expected.equals(text(TITLE)) || !d.findElements(By.cssSelector("[role='alert']")).isEmpty());
        return text(TITLE).equals(expected) ? expected : text(TITLE) + " / " + error();
    }

    /** The green message (role=status) or the red one (role=alert). */
    public String success() {
        return text(By.cssSelector(".alert-success"));
    }

    public String error() {
        return text(By.cssSelector("[role='alert']"));
    }

    public boolean resendOffered() {
        return !driver.findElements(button("Bestätigungs-E-Mail erneut senden")).isEmpty();
    }

    public void waitForPath(String path) {
        waitForUrlEnding(path);
    }
}
