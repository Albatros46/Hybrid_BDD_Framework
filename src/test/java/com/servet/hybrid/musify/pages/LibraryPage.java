package com.servet.hybrid.musify.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.servet.hybrid.framework.web.BasePage;

public class LibraryPage extends BasePage {

    private static final By NEW_NAME = By.cssSelector("input[aria-label='New playlist name']");
    private static final By PUBLIC = By.xpath("//label[contains(@class,'checkbox')]/input");

    public LibraryPage(WebDriver driver) {
        super(driver);
        visible(NEW_NAME);
    }

    public void create(String name, boolean isPublic) {
        type(NEW_NAME, name);
        if (isPublic) {
            click(PUBLIC);
        }
        click(button("Create playlist"));
    }

    /** The small line under a playlist tile, e.g. "Private · Anna". */
    public String subtitleOf(String playlist) {
        return text(By.xpath("//h2[normalize-space()='Your playlists']/following-sibling::div[1]"
                + "//a[.//div[@class='title' and normalize-space()=\"" + playlist + "\"]]//div[contains(@class,'muted')]"));
    }
}
