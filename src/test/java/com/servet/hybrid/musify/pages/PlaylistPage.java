package com.servet.hybrid.musify.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.servet.hybrid.framework.web.BasePage;

public class PlaylistPage extends BasePage {

    private static final By HEADER = By.cssSelector(".playlist-header");
    private static final By TITLES = By.cssSelector(".song-list .song-main .title");

    public PlaylistPage(WebDriver driver) {
        super(driver);
    }

    public PlaylistPage loaded() {
        visible(HEADER);
        return this;
    }

    public String heading() {
        return text(By.cssSelector(".playlist-header h1"));
    }

    /** "Private playlist" / "Public playlist" */
    public String visibility() {
        return text(By.cssSelector(".playlist-header .muted.small"));
    }

    /** "Anna · 2 songs" */
    public String ownerLine() {
        return text(By.xpath("//div[contains(@class,'playlist-header')]//h1/following-sibling::div[1]"));
    }

    /** Read inside the wait: if React replaces the list while it is read, the read is simply repeated. */
    public List<String> songTitles() {
        return wait.until(d -> d.findElements(TITLES).stream().map(WebElement::getText).toList());
    }

    public void remove(String title) {
        int before = songTitles().size();
        click(By.cssSelector("button[aria-label='Remove " + title + "']"));
        wait.until(d -> songTitles().size() == before - 1);
    }

    public void makePublic() {
        click(button("Make public"));
        wait.until(d -> visibility().equals("Public playlist"));
    }

    public void delete() {
        click(button("Delete playlist"));
        acceptConfirm();
        waitForUrlEnding("/library");
    }

    public String error() {
        return text(By.cssSelector("[role='alert']"));
    }
}
