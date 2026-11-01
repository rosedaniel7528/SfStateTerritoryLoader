package listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

import core.ConfigReader;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int count = 0;
    
    // Read from properties file.
    private int maxRetries = Integer.parseInt(ConfigReader.getProperty("retryCount"));
    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);

    @Override
    public boolean retry(ITestResult result) {
        if (!result.isSuccess()) { 
            if (count < maxRetries) {
                count++;
                log.warn("Retrying test: " + result.getMethod().getMethodName() + " (Attempt " + count + ")");
                return true; // Tells TestNG to re-run the test
            }
        }
        return false; // Tells TestNG to stop retrying and fail it
    }
}