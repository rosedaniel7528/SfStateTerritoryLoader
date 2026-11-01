package reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.File;

public class ExtentManager {

    private static ExtentReports extent;

    public static ExtentReports extentReportObject() {
        if (extent == null) {
            // Standardized pipeline folder structure
            String reportPath = System.getProperty("user.dir") + "/reports/Test-Execution-Report.html";
            
            // Create reports folder if it doesn't exist
            File reportDir = new File(System.getProperty("user.dir") + "/reports");
            if (!reportDir.exists()) {
                reportDir.mkdirs();
            }

            ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
            spark.config().setReportName("Salesforce Automation Report");
            spark.config().setDocumentTitle("Pipeline Test Results");
            spark.config().setTheme(Theme.STANDARD); // Or Theme.DARK

            extent = new ExtentReports();
            extent.attachReporter(spark);
            
            // Add system info (In pipelines, you can read these from System.getenv())
            extent.setSystemInfo("Environment", "QA");
            extent.setSystemInfo("Tester", "Rose Daniel");
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        }
        return extent;
    }
}