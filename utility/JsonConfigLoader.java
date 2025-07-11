import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Thread-safe JSON configuration loader that supports loading from external
 * file paths.
 * Provides JSON parsing capabilities with flexible file path configuration.
 * 
 * Usage example:
 * 
 * <pre>
 * // Load from classpath
 * JsonConfigLoader loader = JsonConfigLoader.getInstance();
 * 
 * // Load from external path
 * JsonConfigLoader loader = JsonConfigLoader.getInstance("/path/to/config.json");
 * 
 * String dbUrl = loader.getString("database.url");
 * int poolSize = loader.getInt("database.connection.pool.maxSize", 10);
 * JsonNode dbConfig = loader.getJsonNode("database");
 * </pre>
 */
public class JsonConfigLoader {

    private static final Logger logger = Logger.getLogger(JsonConfigLoader.class.getName());
    private static volatile JsonConfigLoader instance;
    private static final String DEFAULT_CONFIG_FILE = "config.json";

    private final ObjectMapper objectMapper;
    private volatile JsonNode configRoot;
    private volatile String configFilePath;
    private volatile long lastModified;

    // Prevent direct instantiation
    private JsonConfigLoader() {
        this.objectMapper = new ObjectMapper();
        this.configFilePath = null; // Will load from classpath
        loadConfiguration();
    }

    // Constructor for external file path
    private JsonConfigLoader(String externalFilePath) {
        this.objectMapper = new ObjectMapper();
        this.configFilePath = externalFilePath;
        loadConfiguration();
    }

    /**
     * Gets the singleton instance of JsonConfigLoader (loads from classpath).
     * 
     * @return the singleton instance
     */
    public static JsonConfigLoader getInstance() {
        if (instance == null) {
            synchronized (JsonConfigLoader.class) {
                if (instance == null) {
                    instance = new JsonConfigLoader();
                }
            }
        }
        return instance;
    }

    /**
     * Gets the singleton instance of JsonConfigLoader with external file path.
     * 
     * @param externalFilePath the absolute path to the JSON configuration file
     * @return the singleton instance
     */
    public static JsonConfigLoader getInstance(String externalFilePath) {
        if (instance == null) {
            synchronized (JsonConfigLoader.class) {
                if (instance == null) {
                    instance = new JsonConfigLoader(externalFilePath);
                }
            }
        }
        return instance;
    }

    /**
     * Loads configuration from JSON file.
     */
    private void loadConfiguration() {
        try {
            if (configFilePath != null) {
                loadFromExternalFile();
            } else {
                loadFromClasspath();
            }
            logger.info("JSON configuration loaded successfully from: " +
                    (configFilePath != null ? configFilePath : "classpath"));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load JSON configuration", e);
            throw new RuntimeException("Configuration initialization failed", e);
        }
    }

    /**
     * Loads configuration from JSON file in classpath.
     */
    private void loadFromClasspath() throws IOException {
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(DEFAULT_CONFIG_FILE)) {

            if (inputStream == null) {
                logger.warning("JSON config file not found: " + DEFAULT_CONFIG_FILE + ", using empty configuration");
                configRoot = objectMapper.createObjectNode();
                return;
            }

            configRoot = objectMapper.readTree(inputStream);
            lastModified = System.currentTimeMillis();
            logger.fine("Loaded JSON configuration from classpath: " + DEFAULT_CONFIG_FILE);
        }
    }

    /**
     * Loads configuration from external file path.
     */
    private void loadFromExternalFile() throws IOException {
        File configFile = new File(configFilePath);

        if (!configFile.exists()) {
            logger.warning("External config file not found: " + configFilePath + ", using empty configuration");
            configRoot = objectMapper.createObjectNode();
            lastModified = 0;
            return;
        }

        if (!configFile.canRead()) {
            throw new IOException("Cannot read configuration file: " + configFilePath);
        }

        try (FileInputStream inputStream = new FileInputStream(configFile)) {
            configRoot = objectMapper.readTree(inputStream);
            lastModified = configFile.lastModified();
            logger.fine("Loaded JSON configuration from external file: " + configFilePath);
        }
    }

    /**
     * Checks if the external file has been modified and reloads if necessary.
     */
    private void checkAndReloadIfModified() {
        if (configFilePath != null) {
            File configFile = new File(configFilePath);
            if (configFile.exists() && configFile.lastModified() > lastModified) {
                synchronized (this) {
                    if (configFile.lastModified() > lastModified) {
                        try {
                            loadFromExternalFile();
                            logger.info("Configuration automatically reloaded due to file modification");
                        } catch (IOException e) {
                            logger.log(Level.WARNING, "Failed to reload modified configuration file", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Gets a string value from configuration.
     * Supports dot notation for nested JSON properties.
     * 
     * @param key the configuration key (supports dot notation for nested JSON)
     * @return the string value, or null if not found
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Gets a string value with default fallback.
     * 
     * @param key          the configuration key
     * @param defaultValue the default value if not found
     * @return the string value or default value
     */
    public String getString(String key, String defaultValue) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Configuration key cannot be null or empty");
        }

        // Check if file has been modified and reload if necessary
        checkAndReloadIfModified();

        // Get value from JSON configuration
        JsonNode node = getJsonNodeByPath(key);
        if (node != null && !node.isNull()) {
            return node.asText();
        }

        return defaultValue;
    }

    /**
     * Gets an integer value from configuration.
     * 
     * @param key          the configuration key
     * @param defaultValue the default value if not found or invalid
     * @return the integer value or default value
     */
    public int getInt(String key, int defaultValue) {
        try {
            String value = getString(key);
            return value != null ? Integer.parseInt(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warning("Invalid integer value for key '" + key + "': " + getString(key));
            return defaultValue;
        }
    }

    /**
     * Gets a boolean value from configuration.
     * 
     * @param key          the configuration key
     * @param defaultValue the default value if not found
     * @return the boolean value or default value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value.trim()) || "1".equals(value.trim());
    }

    /**
     * Gets a long value from configuration.
     * 
     * @param key          the configuration key
     * @param defaultValue the default value if not found or invalid
     * @return the long value or default value
     */
    public long getLong(String key, long defaultValue) {
        try {
            String value = getString(key);
            return value != null ? Long.parseLong(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warning("Invalid long value for key '" + key + "': " + getString(key));
            return defaultValue;
        }
    }

    /**
     * Gets a double value from configuration.
     * 
     * @param key          the configuration key
     * @param defaultValue the default value if not found or invalid
     * @return the double value or default value
     */
    public double getDouble(String key, double defaultValue) {
        try {
            String value = getString(key);
            return value != null ? Double.parseDouble(value.trim()) : defaultValue;
        } catch (NumberFormatException e) {
            logger.warning("Invalid double value for key '" + key + "': " + getString(key));
            return defaultValue;
        }
    }

    /**
     * Gets a JsonNode for complex configuration objects.
     * 
     * @param key the configuration key
     * @return the JsonNode, or null if not found
     */
    public JsonNode getJsonNode(String key) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }

        // Check if file has been modified and reload if necessary
        checkAndReloadIfModified();

        // Get JsonNode from configuration
        return getJsonNodeByPath(key);
    }

    /**
     * Gets a JsonNode by dot-notation path (e.g., "database.connection.pool").
     * 
     * @param path the dot-notation path
     * @return the JsonNode at the path, or null if not found
     */
    private JsonNode getJsonNodeByPath(String path) {
        if (configRoot == null) {
            return null;
        }

        JsonNode current = configRoot;
        String[] parts = path.split("\\.");

        for (String part : parts) {
            if (current == null || !current.has(part)) {
                return null;
            }
            current = current.get(part);
        }

        return current;
    }

    /**
     * Checks if a configuration key exists.
     * 
     * @param key the configuration key
     * @return true if the key exists, false otherwise
     */
    public boolean containsKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            return false;
        }

        // Check if file has been modified and reload if necessary
        checkAndReloadIfModified();

        // Check JSON configuration
        JsonNode node = getJsonNodeByPath(key);
        return node != null && !node.isNull();
    }

    /**
     * Reloads configuration from JSON file.
     */
    public synchronized void reload() {
        try {
            loadConfiguration();
            logger.info("JSON configuration reloaded successfully");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reload JSON configuration", e);
            throw new RuntimeException("Configuration reload failed", e);
        }
    }

    /**
     * Gets the current configuration file path.
     * 
     * @return the configuration file path, or null if loading from classpath
     */
    public String getConfigFilePath() {
        return configFilePath;
    }

    /**
     * Gets the last modified timestamp of the configuration.
     * 
     * @return the last modified timestamp
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * Checks if the configuration is loaded from an external file.
     * 
     * @return true if loaded from external file, false if from classpath
     */
    public boolean isExternalFile() {
        return configFilePath != null;
    }
}
