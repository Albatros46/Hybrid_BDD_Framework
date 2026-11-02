package com.servet.hybrid.framework.web;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

/** Waits and locators shared by the page objects of both applications. */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        // React re-renders lists after every change: an element found a moment ago may be replaced
        this.wait = (WebDriverWait) new WebDriverWait(driver, Duration.ofSeconds(10))
                .ignoring(StaleElementReferenceException.class);
    }

    /** A form field by the text of the label around it: what the user reads, not a CSS class. */
    protected static By field(String label) {
        return By.xpath("//label[normalize-space(text()[1])='" + label + "']//*[self::input or self::select]");
    }

    protected static By button(String text) {
        return By.xpath("//button[normalize-space()='" + text + "']");
    }

    protected WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void type(By locator, String text) {
        WebElement element = visible(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected void choose(By locator, String visibleText) {
        wait.until(d -> new Select(d.findElement(locator)).getOptions().stream()
                .anyMatch(o -> o.getText().equals(visibleText)));
        new Select(visible(locator)).selectByVisibleText(visibleText);
    }

    protected List<String> options(By locator) {
        return new Select(visible(locator)).getOptions().stream().map(WebElement::getText).toList();
    }

    protected String text(By locator) {
        return visible(locator).getText().replace('\u00a0', ' ').trim();
    }

    /** window.confirm(...) of the app: accept it like a user clicking "OK". */
    protected void acceptConfirm() {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    protected void waitForUrlEnding(String path) {
        wait.until(d -> d.getCurrentUrl().replaceAll("[?#].*$", "").endsWith(path));
    }
}
