package pages;

import core.DriverManager;
import utils.ExcelDataReader;
import utils.WaitUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

public class BasePage {
    protected WebDriver driver;
    protected WaitUtils wait;
    protected ExcelDataReader excelDataReader;
    protected final Logger log = LogManager.getLogger(getClass());
    protected JavascriptExecutor js;
    
    public BasePage() {
        // Automatically fetch the thread-safe driver!
        this.driver = DriverManager.getDriver();
        this.wait = new WaitUtils();
        this.js = (JavascriptExecutor) driver;
    }
    
    // Add common click(), type(), waitForVisibility() methods here
}