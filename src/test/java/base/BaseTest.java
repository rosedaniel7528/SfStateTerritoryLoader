package base;

import core.ConfigReader;
import core.DriverFactory;
import core.DriverManager;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {
	
	private static final Logger log = LogManager.getLogger(BaseTest.class);

    @BeforeMethod
    public void setup(Method method) {
    	ThreadContext.put("test", method.getName());
        String browser = ConfigReader.getProperty("browser");
        String url = ConfigReader.getProperty("url");
        String downloadPath = System.getProperty("user.dir") + "/downloads";
        		
        log.info("Starting browser {}",browser);
        // 1. Create Driver
        WebDriver driver = DriverFactory.createDriver(browser,downloadPath);
        
        // 2. Set it to ThreadLocal
        DriverManager.setDriver(driver);

        DriverManager.getDriver().manage().window().maximize();
        log.info("Navigating to the url {}",url);
        DriverManager.getDriver().get(url);
    }

    @AfterMethod
    public void teardown() {
    	log.info("Closing the browser");
        DriverManager.quitDriver();
    	ThreadContext.clearMap();
    }
}