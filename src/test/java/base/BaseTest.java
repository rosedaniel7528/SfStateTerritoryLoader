package base;

import core.ConfigReader;
import core.DriverFactory;
import core.DriverManager;
import core.SessionLoader;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Objects;
import java.util.concurrent.Semaphore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {
	
	private static final Logger log = LogManager.getLogger(BaseTest.class);
	private static final Semaphore BROWSER_SLOTS = new Semaphore(
	        Integer.parseInt(Objects.requireNonNullElse(ConfigReader.getProperty("parallelism"), "4")));
	private static final ThreadLocal<Boolean> hasSlot = ThreadLocal.withInitial(() -> false);

    @BeforeMethod
    public void setup(Method method,Object[] params) throws IOException {
    	BROWSER_SLOTS.acquireUninterruptibly();
    	hasSlot.set(true);
    	ThreadContext.put("test", method.getName()+(params.length > 0 ? "-" + params[0] : ""));
        String browser = ConfigReader.getProperty("browser");
        String url = ConfigReader.getRequired("url");
        String downloadPath = System.getProperty("user.dir") + "/downloads";
        		
        log.info("Starting browser {}",browser);
        // 1. Create Driver
        WebDriver driver = DriverFactory.createDriver(browser,downloadPath);
        
        // 2. Set it to ThreadLocal
        DriverManager.setDriver(driver);

        String gridUrl = ConfigReader.getProperty("gridUrl");
        if (gridUrl == null || gridUrl.isBlank()) {
            DriverManager.getDriver().manage().window().maximize();
        }
        log.info("Navigating to host {}", URI.create(url).getHost());
        if (SessionLoader.hasSavedSession()) {
            SessionLoader.apply(DriverManager.getDriver(), url);
        } else {
            DriverManager.getDriver().get(url);
        }
    }

    @AfterMethod(alwaysRun = true)
    public void teardown() {
    	log.info("Closing the browser");
        DriverManager.quitDriver();
        if (hasSlot.get()) {
            BROWSER_SLOTS.release();
            hasSlot.remove();
        }
    	ThreadContext.clearMap();
    }
}