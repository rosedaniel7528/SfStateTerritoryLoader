package utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import core.ConfigReader;
import core.DriverManager;

public class WaitUtils {
	
	WebDriver driver;
	int timeOut;
	

	public WaitUtils() {
		this.driver = DriverManager.getDriver();
		String configTimeout = ConfigReader.getProperty("explicitTimeout");
		this.timeOut= (configTimeout != null) ? Integer.parseInt(configTimeout) : 20; 
	}

	public WebDriverWait getWait() {
		return new WebDriverWait(driver, Duration.ofSeconds(timeOut));
	}
	
	public WebDriverWait getLongWait() {
	    int longTimeout = Integer.parseInt(ConfigReader.getProperty("longTimeout"));
	    return new WebDriverWait(driver, Duration.ofSeconds(longTimeout));
	}
	
	public void elementToBeClickable(By locator) {
		getWait().until(ExpectedConditions.elementToBeClickable(locator));
	}

	public void elementToBeClickable(WebElement element) {
		getWait().until(ExpectedConditions.elementToBeClickable(element));
	}

	public void presenceOfElementLocated(By locator) {
		getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
	}

	public void visibilityOf(WebElement element) {
		getWait().until(ExpectedConditions.visibilityOf(element));
	}
	
	public void visibilityOf(By element) {
		getWait().until(ExpectedConditions.visibilityOfElementLocated(element));
	}
	
	public void invisibilityOf(WebElement element) {
		getWait().until(ExpectedConditions.invisibilityOf(element));
	}

	public void visibilityOfAllElements(List<WebElement> element) {
		getWait().until(ExpectedConditions.visibilityOfAllElements(element));
	}
	
	public void visibilityOfAllElements(By element) {
		getWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(element));
	}
	
	public void frameToBeAvailableAndSwitchToIt(By element) {
		getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(element));
	}
	
	public void invisibilityOfElementLocated(By element) {
		getWait().until(ExpectedConditions.invisibilityOfElementLocated(element));
	}

	public void frameToBeAvailableAndSwitchToIt(WebElement element) {
		getWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(element));		
	}
		
}
