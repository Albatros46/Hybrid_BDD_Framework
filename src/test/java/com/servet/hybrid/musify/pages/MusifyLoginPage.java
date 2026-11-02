package com.servet.hybrid.musify.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.servet.hybrid.framework.web.BasePage;

public class MusifyLoginPage extends BasePage {

    public MusifyLoginPage(WebDriver driver) {
        super(driver);
    }

    public MusifyLoginPage open(String appUrl) {
        driver.get(appUrl + "/login");
        visible(button("Log in"));
        return this;
    }

    public void logIn(String email, String password) {
        type(field("Email"), email);
        type(field("Password"), password);
        click(button("Log in"));
    }

    public String error() {
        return text(By.cssSelector("[role='alert']"));
    }
}
