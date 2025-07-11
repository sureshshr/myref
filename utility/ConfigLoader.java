import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Thread-safe configuration loader that loads properties from classpath.
 * Supports multiple property files and provides caching for performance.
 * 
 * Usage example:
 * <pre>
 * String dbUrl = ConfigLoader.get("db.url");
 * int timeout = ConfigLoader.getInt("connection.timeout", 30);
 * </pre>
 */
public class ConfigLoader {

    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());
    private static final String[] DEFAULT_PROPERTY_FILES = {"db.properties", "app.properties"};
    private static volatile Properties props;

    // Prevent instantiation
    private ConfigLoader() {}

    public static Properties get() throws IOException {
        if (props == null) {
            synchronized (ConfigLoader.class) {
                if (props == null) {
                    props = new Properties();
                    for (String fileName : DEFAULT_PROPERTY_FILES) {
                        loadFromClasspath(fileName);
                    }
                    logger.info("Configuration loaded successfully");
                }
            }
        }
        return props;
    }

    private static void loadFromClasspath(String fileName) throws IOException {
        try (InputStream in = Thread.currentThread()
                                    .getContextClassLoader()
                                    .getResourceAsStream(fileName)) {
            if (in == null) {
                logger.warning("Properties file not found: " + fileName);
                throw new IOException("Properties file not found: " + fileName);
            }
            props.load(in);
            logger.fine("Loaded properties from: " + fileName);
        }
    }

    /**
     * Gets a property value by key.
     * @param key the property key
     * @return the property value, or null if not found
     * @throws RuntimeException if configuration cannot be loaded
     */
    public static String get(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Property key cannot be null or empty");
        }
        try {
            return get().getProperty(key);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load configuration", e);
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    /**
     * Gets a property value with a default fallback.
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the property value or default value
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets an integer property value.
     * @param key the property key
     * @param defaultValue the default value if property is not found or invalid
     * @return the integer property value or default value
     */
    public static int getInt(String key, int defaultValue) {
        try {
            String value = get(key);
            return value != null ? Integer.parseInt(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warning("Invalid integer value for key '" + key + "': " + get(key));
            return defaultValue;
        }
    }

    /**
     * Gets a boolean property value.
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the boolean property value or default value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
    }

    /**
     * Checks if a property key exists.
     * @param key the property key to check
     * @return true if the key exists, false otherwise
     */
    public static boolean containsKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        try {
            return get().containsKey(key);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to check property key: " + key, e);
            return false;
        }
    }

    /**
     * Gets a long property value.
     * @param key the property key
     * @param defaultValue the default value if property is not found or invalid
     * @return the long property value or default value
     */
    public static long getLong(String key, long defaultValue) {
        try {
            String value = get(key);
            return value != null ? Long.parseLong(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warning("Invalid long value for key '" + key + "': " + get(key));
            return defaultValue;
        }
    }

    /**
     * Reloads the configuration from classpath.
     * @throws IOException if properties cannot be loaded
     */
    public static void reload() throws IOException {
        synchronized (ConfigLoader.class) {
            props = new Properties();
            for (String fileName : DEFAULT_PROPERTY_FILES) {
                loadFromClasspath(fileName);
            }
            logger.info("Configuration reloaded successfully");
        }
    }
}
