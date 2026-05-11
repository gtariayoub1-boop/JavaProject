package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppSecrets {

    private static final Properties PROPERTIES = loadProperties();

    private AppSecrets() {
    }

    public static String get(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }

        String envKey = key.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return PROPERTIES.getProperty(key, "").trim();
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key);
        if (value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = AppSecrets.class.getResourceAsStream("/app-secrets.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("Unable to load app secrets: " + e.getMessage());
        }
        return properties;
    }
}
