package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import core.DriverManager;

public class ScreenshotUtils {

    /*
     * Enterprise Standard: Returns Base64 String instead of a file path.
     * This embeds the image directly into the Extent HTML report.
     * No broken image links when sending reports via email!
     */
    public static String getBase64Screenshot() {
        if (DriverManager.getDriver() != null) {
            return ((TakesScreenshot) DriverManager.getDriver()).getScreenshotAs(OutputType.BASE64);
        }
        return "";
    }
}