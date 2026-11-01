package pages.common;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

import flows.StateLoadFlow;
import pages.BasePage;

public class LoginPage extends BasePage {
	
	
	private final By userName = By.id("username");
	private final By passWord = By.id("password");
	private final By loginButton = By.id("Login");
	
    // Fluent design: Logging in returns the Header component
    public StateLoadFlow login(String username, String password) {
    	wait.elementToBeClickable(userName);
        driver.findElement(userName).sendKeys(username);
        try {
        driver.findElement(passWord).sendKeys(password);
        }catch(NoSuchElementException e) {
        	log.warn("Username and password field are not present in the same screen");
        	driver.findElement(loginButton).click();
            wait.elementToBeClickable(passWord);
            driver.findElement(passWord).sendKeys(password);
        }
        driver.findElement(loginButton).click();
		log.info("logging in");
        return new StateLoadFlow();
    }
}