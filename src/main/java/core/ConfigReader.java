package core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties prop = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getResourceAsStream("/config.properties")) {
            if (in == null) throw new IllegalStateException("config.properties not found on classpath");
            prop.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load config.properties", e);
        }
    }

    public static String getProperty(String key) {
        String sys = System.getProperty(key);                       // -Dusername=...
        if (sys != null && !sys.isBlank()) return sys;
        String env = System.getenv("SF_" + key.toUpperCase());      // SF_USERNAME ...
        if (env != null && !env.isBlank()) return env;
        return prop.getProperty(key);                                // config.properties
    }

    public static String getRequired(String key) {
        String v = getProperty(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing '" + key + "'. Set env var SF_"
                    + key.toUpperCase() + " or pass -D" + key + "=...");
        }
        return v;
    }
}