package com.servet.hybrid.netflix.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.servet.hybrid.framework.web.BasePage;

/** The film overview with search and the "+ Meine Liste" buttons; also used for "Meine Liste". */
public class BrowsePage extends BasePage {

    private static final By SEARCH = By.cssSelector("input.search");
    private static final By CARDS = By.cssSelector(".video-card");

    public BrowsePage(WebDriver driver) {
        super(driver);
    }

    /** Types the title and waits until the (debounced) search shows either cards or "Keine Filme gefunden." */
    public BrowsePage search(String text) {
        type(SEARCH, text);
        wait.until(d -> d.getPageSource().contains("Keine Filme gefunden.")
                || d.findElements(By.cssSelector(".video-card[aria-label=\"" + text + "\"]")).size() > 0);
        return this;
    }

    public List<String> titles() {
        return wait.until(d -> d.findElements(CARDS).stream().map(c -> c.getAttribute("aria-label")).toList());
    }

    public boolean nothingFound() {
        return driver.getPageSource().contains("Keine Filme gefunden.");
    }

    /** Opens the detail dialog of the film and clicks "+ Meine Liste"; waits for "✓ In Meiner Liste". */
    public void addToMyList(String title) {
        click(By.cssSelector(".video-card[aria-label=\"" + title + "\"]"));
        By dialog = By.cssSelector("[role='dialog'][aria-label=\"" + title + "\"]");
        visible(dialog);
        click(By.xpath("//div[@role='dialog']//button[normalize-space()='+ Meine Liste']"));
        visible(By.xpath("//div[@role='dialog']//button[normalize-space()='✓ In Meiner Liste']"));
    }

    public List<String> myList() {
        visible(By.xpath("//h1[normalize-space()='Meine Liste']"));
        wait.until(d -> d.findElements(By.cssSelector(".spinner")).isEmpty());
        return driver.findElements(CARDS).stream().map(c -> c.getAttribute("aria-label")).toList();
    }
}
