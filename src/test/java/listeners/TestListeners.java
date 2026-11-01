package listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;

import reporting.ExtentManager;
import utils.ScreenshotUtils;

public class TestListeners implements ITestListener {

    // ThreadLocal ensures parallel execution does not mix up logs
    private static ExtentReports extent = ExtentManager.extentReportObject();
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();
    private static final Logger log = LogManager.getLogger(TestListeners.class);

    @Override
    public void onTestStart(ITestResult result) {
    	log.info("Test Starter: {}",result.getName());
        ExtentTest test = extent.createTest(result.getTestClass().getName() + " - " + result.getMethod().getMethodName());
        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    	log.info("Test Success: {}",result.getName());
        extentTest.get().log(Status.PASS, MarkupHelper.createLabel("Test Passed", ExtentColor.GREEN));
    }

    @Override
    public void onTestFailure(ITestResult result) {
    	log.error("Test Failed: {}",result.getName(),result.getThrowable());
        extentTest.get().log(Status.FAIL, MarkupHelper.createLabel("Test Failed", ExtentColor.RED));
        extentTest.get().fail(result.getThrowable()); // Logs the exact error (e.g. NoSuchElement)

        // Attach Base64 Screenshot directly to report
        String base64Screenshot = ScreenshotUtils.getBase64Screenshot();
        if (!base64Screenshot.isEmpty()) {
            extentTest.get().addScreenCaptureFromBase64String(base64Screenshot, "Failure Screenshot");
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // This is crucial for RetryAnalyzer! 
        // When a test fails and retries, the first attempt is marked as 'Skipped'.
    	log.info("Test Skipped: {}",result.getName());
        extentTest.get().log(Status.SKIP, MarkupHelper.createLabel("Test Skipped / Retrying", ExtentColor.ORANGE));
        extentTest.get().skip(result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
    	log.info("Test Execution Finished");
        extent.flush(); // Writes everything to the HTML file
        extentTest.remove(); // Prevent memory leaks in CI/CD
    }
}