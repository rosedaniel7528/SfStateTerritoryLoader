package core;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.RemoteWebDriver;

public class DriverFactory {

    public static WebDriver createDriver(String browser, String downloadPath) {
    	
    	boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless"));
    	String gridUrl = ConfigReader.getProperty("gridUrl");

    	if (browser.equalsIgnoreCase("chrome")) {

            ChromeOptions options = new ChromeOptions();

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_setting_values.notifications", 2);
            prefs.put("profile.default_content_setting_values.geolocation", 2);
            prefs.put("download.default_directory", downloadPath);
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            prefs.put("autofill.profile_enabled", false);
            prefs.put("autofill.address_enabled", false);
            
            if (headless) {
                options.addArguments("--headless=new", "--window-size=1920,1080");
            }

            options.setExperimentalOption("prefs", prefs);
            options.setAcceptInsecureCerts(true);
            
            if (gridUrl != null && !gridUrl.isBlank()) {
            	options.addArguments("--window-size=1920,1080");
                return remote(options, gridUrl);
            }

            return new ChromeDriver(options);

        }

        else if (browser.equalsIgnoreCase("edge")) {

            EdgeOptions options = new EdgeOptions();

            Map<String, Object> prefs = new HashMap<>();
            prefs.put("profile.default_content_setting_values.notifications", 2);
            prefs.put("profile.default_content_setting_values.geolocation", 2);
            prefs.put("download.default_directory", downloadPath);
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            prefs.put("autofill.profile_enabled", false);
            prefs.put("autofill.address_enabled", false);
            if (headless) {
                options.addArguments("--headless=new", "--window-size=1920,1080");
            }

            options.setExperimentalOption("prefs", prefs);
            
            if (gridUrl != null && !gridUrl.isBlank()) {
            	options.addArguments("--window-size=1920,1080");
                return remote(options, gridUrl);
            }

            return new EdgeDriver(options);
        }

        throw new IllegalArgumentException("Unsupported Browser: " + browser);
    }
    
    private static WebDriver remote(MutableCapabilities options, String gridUrl) {
        try {
        	WebDriver driver = new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
        	try {
        	    return new Augmenter().augment(driver);
        	} catch (RuntimeException e) {
        	    driver.quit();      // don't leave the session running on the node
        	    throw e;
        	}        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid gridUrl: " + gridUrl, e);
        }
    }
}