import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Thread-safe singleton configuration loader that loads properties from
 * classpath.
 * Supports multiple property files and provides caching for performance.
 * Java 8 compatible implementation.
 * 
 * Usage example:
 * 
 * <pre>
 * ConfigLoader config = ConfigLoader.getInstance();
 * String dbUrl = config.get("db.url");
 * int timeout = config.getInt("connection.timeout", 30);
 * </pre>
 */
public class ConfigLoader {

    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());
    private static final String[] DEFAULT_PROPERTY_FILES = { "db.properties", "app.properties" };

    // Singleton instance
    private static volatile ConfigLoader instance;

    // Instance properties
    private Properties props;

    // Prevent instantiation
    private ConfigLoader() {
        loadConfiguration();
    }

    /**
     * Gets the singleton instance of ConfigLoader.
     * 
     * @return the singleton instance
     */
    public static ConfigLoader getInstance() {
        if (instance == null) {
            synchronized (ConfigLoader.class) {
                if (instance == null) {
                    instance = new ConfigLoader();
                }
            }
        }
        return instance;
    }

    /**
     * Loads configuration from properties files.
     */
    private void loadConfiguration() {
        this.props = new Properties();
        try {
            for (String fileName : DEFAULT_PROPERTY_FILES) {
                loadFromClasspath(fileName);
            }
            logger.info("Configuration loaded successfully");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load configuration", e);
            throw new RuntimeException("Configuration initialization failed", e);
        }
    }

    /**
     * Loads a properties file from classpath.
     * 
     * @param fileName the name of the properties file
     * @throws IOException if file cannot be read
     */
    private void loadFromClasspath(String fileName) throws IOException {
        try (InputStream in = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(fileName)) {
            if (in == null) {
                logger.warning("Properties file not found: " + fileName);
                return; // Don't throw exception, just skip missing files
            }
            props.load(in);
            logger.fine("Loaded properties from: " + fileName);
        }
    }

    /**
     * Gets the Properties object.
     * 
     * @return the Properties object
     */
    public Properties getProperties() {
        return props;
    }

    /**
     * Gets a property value by key.
     * 
     * @param key the property key
     * @return the property value, or null if not found
     */
    public String get(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Property key cannot be null or empty");
        }
        return props.getProperty(key);
    }

    /**
     * Gets a property value with a default fallback.
     * 
     * @param key          the property key
     * @param defaultValue the default value if property is not found
     * @return the property value or default value
     */
    public String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    /**
     * Gets an integer property value.
     * 
     * @param key          the property key
     * @param defaultValue the default value if property is not found or invalid
     * @return the integer property value or default value
     */
    public int getInt(String key, int defaultValue) {
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
     * 
     * @param key          the property key
     * @param defaultValue the default value if property is not found
     * @return the boolean property value or default value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
    }

    /**
     * Gets a long property value.
     * 
     * @param key          the property key
     * @param defaultValue the default value if property is not found or invalid
     * @return the long property value or default value
     */
    public long getLong(String key, long defaultValue) {
        try {
            String value = get(key);
            return value != null ? Long.parseLong(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warning("Invalid long value for key '" + key + "': " + get(key));
            return defaultValue;
        }
    }

    /**
     * Gets a double property value.
     * 
     * @param key          the property key
     * @param defaultValue the default value if property is not found or invalid
     * @return the double property value or default value
     */
    public double getDouble(String key, double defaultValue) {
        try {
            String value = get(key);
            return value != null ? Double.parseDouble(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warning("Invalid double value for key '" + key + "': " + get(key));
            return defaultValue;
        }
    }

    /**
     * Checks if a property key exists.
     * 
     * @param key the property key to check
     * @return true if the key exists, false otherwise
     */
    public boolean containsKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }
        return props.containsKey(key);
    }

    /**
     * Reloads the configuration from classpath.
     */
    public synchronized void reload() {
        try {
            loadConfiguration();
            logger.info("Configuration reloaded successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reload configuration", e);
            throw new RuntimeException("Configuration reload failed", e);
        }
    }
}
