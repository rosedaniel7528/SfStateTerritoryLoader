package tools;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.HasCdp;

import com.fasterxml.jackson.databind.ObjectMapper;

import core.ConfigReader;
import core.DriverFactory;

/**
 * Run this once as a Java Application (not through TestNG).
 * It opens a visible Chrome, waits while you log in by hand (OTP included),
 * then saves the Salesforce session cookies to session/cookies.json for SessionLoader.
 * The saved file is a login: keep it out of Git and never share it.
 */
public class SessionCapture {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {

        // The login has to be done by hand, so the browser must be visible.
        System.setProperty("headless", "false");

        String url = ConfigReader.getRequired("url");
        WebDriver driver = DriverFactory.createDriver("chrome", System.getProperty("user.dir") + "/downloads");

        try {
            driver.get(url);
            System.out.println(">>> Log in by hand (OTP too). When the State and Country page is visible, "
                    + "click in this console and press Enter.");
            System.in.read();

            // Ask Chrome for every cookie in this browser, whatever domain it belongs to.
            Map<String, Object> result = ((HasCdp) driver).executeCdpCommand("Network.getAllCookies", new HashMap<>());
            List<Map<String, Object>> all = (List<Map<String, Object>>) result.get("cookies");

            // Keep only Salesforce cookies.
            List<Map<String, Object>> saved = new ArrayList<>();
            for (Map<String, Object> c : all) {
                String domain = String.valueOf(c.get("domain"));
                if (domain.contains("salesforce") || domain.contains("force.com")) {
                    saved.add(c);
                }
            }

            File out = new File(System.getProperty("user.dir") + "/session/cookies.json");
            out.getParentFile().mkdirs();
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(out, saved);

            System.out.println(">>> Saved " + saved.size() + " cookies for domains "
                    + saved.stream().map(m -> (String) m.get("domain")).distinct().toList()
                    + " to " + out.getPath());
        } finally {
            driver.quit();
        }
    }
}
