package core;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.HasCdp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Loads a previously saved Salesforce session (cookies) into a browser so the
 * test can skip the login page. The file is created by tools.SessionCapture.
 * Cookies are set through the Chrome DevTools protocol, which works for any
 * domain without first visiting it.
 * Never log cookie values: they are as sensitive as a password.
 */
public class SessionLoader {

    private static final Logger log = LogManager.getLogger(SessionLoader.class);

    private static final File FILE = new File(System.getProperty("user.dir") + "/session/cookies.json");

    public static boolean hasSavedSession() {
        return FILE.exists();
    }

    public static void apply(WebDriver driver, String targetUrl) throws IOException {

        List<Map<String, Object>> saved = new ObjectMapper().readValue(
                FILE, new TypeReference<List<Map<String, Object>>>() {});

        HasCdp cdp = (HasCdp) driver;
        cdp.executeCdpCommand("Network.enable", new HashMap<>());

        int accepted = 0;

        for (Map<String, Object> c : saved) {

            if (c.get("domain") == null) {
                throw new IllegalStateException(
                        "session/cookies.json is in an old format. Delete it and run tools.SessionCapture again.");
            }

            Map<String, Object> params = new HashMap<>();
            params.put("name", c.get("name"));
            params.put("value", c.get("value"));
            params.put("domain", c.get("domain"));
            params.put("path", c.get("path"));
            params.put("secure", c.get("secure"));
            params.put("httpOnly", c.get("httpOnly"));

            if (c.get("sameSite") != null) {
                params.put("sameSite", c.get("sameSite"));
            }

            // Session cookies have no expiry (CDP reports -1); only copy real expiry times.
            boolean isSession = Boolean.TRUE.equals(c.get("session"));
            if (!isSession && c.get("expires") instanceof Number expires && expires.doubleValue() > 0) {
                params.put("expires", expires.doubleValue());
            }

            Map<String, Object> result = cdp.executeCdpCommand("Network.setCookie", params);

            if (Boolean.TRUE.equals(result.get("success"))) {
                accepted++;
            } else {
                log.warn("Browser rejected cookie '{}' for domain {}", c.get("name"), c.get("domain"));
            }
        }

        log.info("Loaded {} of {} saved cookies", accepted, saved.size());

        driver.get(targetUrl);
    }
}
