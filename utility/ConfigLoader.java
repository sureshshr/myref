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
    private static final String[] RESOURCE_PREFIXES = { "", "resources/", "./resources/", "/resources/", "META-INF/",
            "WEB-INF/classes/" };

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
     * Tries multiple resource paths to support EAR modules and different deployment
     * scenarios.
     * 
     * @param fileName the name of the properties file
     * @throws IOException if file cannot be read
     */
    private void loadFromClasspath(String fileName) throws IOException {
        InputStream inputStream = null;
        boolean fileLoaded = false;

        // Try multiple classloaders and resource paths
        ClassLoader[] classLoaders = {
                Thread.currentThread().getContextClassLoader(),
                ConfigLoader.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };

        for (ClassLoader classLoader : classLoaders) {
            if (classLoader == null)
                continue;

            // Try different resource prefixes
            for (String prefix : RESOURCE_PREFIXES) {
                String resourcePath = prefix + fileName;

                try {
                    inputStream = classLoader.getResourceAsStream(resourcePath);
                    if (inputStream != null) {
                        props.load(inputStream);
                        logger.info("Loaded properties from: " + resourcePath + " using "
                                + classLoader.getClass().getSimpleName());
                        fileLoaded = true;
                        break;
                    }
                } catch (IOException e) {
                    logger.error("Failed to load from path: " + resourcePath + " - " + e.getMessage());
                } finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            logger.error("Error closing stream: " + e.getMessage());
                        }
                        inputStream = null;
                    }
                }
            }

            if (fileLoaded) {
                break;
            }
        }

        // Try loading from file system as fallback (for local development)
        if (!fileLoaded) {
            fileLoaded = tryLoadFromFileSystem(fileName);
        }

        if (!fileLoaded) {
            logger.error("Properties file not found in any location: " + fileName);
            // Don't throw exception, just skip missing files for EAR compatibility
        }
    }

    /**
     * Tries to load properties from file system (fallback for local development).
     * 
     * @param fileName the name of the properties file
     * @return true if file was loaded successfully, false otherwise
     */
    private boolean tryLoadFromFileSystem(String fileName) {
        String[] localPaths = {
                fileName,
                "src/main/resources/" + fileName,
                "resources/" + fileName,
                "./resources/" + fileName,
                "../resources/" + fileName,
                "config/" + fileName,
                "./config/" + fileName
        };

        for (String path : localPaths) {
            java.io.File file = new java.io.File(path);
            if (file.exists() && file.canRead()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {
                    props.load(fis);
                    logger.info("Loaded properties from file system: " + file.getAbsolutePath());
                    return true;
                } catch (IOException e) {
                    logger.error("Failed to load from file system path: " + path + " - " + e.getMessage());
                }
            }
        }
        return false;
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
            logger.error("Invalid integer value for key '" + key + "': " + get(key));
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
            logger.error("Invalid long value for key '" + key + "': " + get(key));
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
            logger.error("Invalid double value for key '" + key + "': " + get(key));
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

    /**
     * Gets detailed information about loaded configuration files.
     * Useful for debugging configuration loading issues in EAR deployments.
     * 
     * @return information about configuration loading
     */
    public String getConfigurationInfo() {
        StringBuilder info = new StringBuilder();
        info.append("ConfigLoader Information:\n");
        info.append("========================\n");
        info.append("Property files to load: ").append(java.util.Arrays.toString(DEFAULT_PROPERTY_FILES)).append("\n");
        info.append("Total properties loaded: ").append(props.size()).append("\n");

        // Show classloader hierarchy
        info.append("ClassLoader hierarchy:\n");
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        int level = 0;
        while (cl != null && level < 5) {
            info.append("  ").append("  ".repeat(level)).append(cl.getClass().getName()).append("\n");
            cl = cl.getParent();
            level++;
        }

        // Show resource search paths that will be tried
        info.append("Resource search paths:\n");
        for (String file : DEFAULT_PROPERTY_FILES) {
            for (String prefix : RESOURCE_PREFIXES) {
                info.append("  ").append(prefix).append(file).append("\n");
            }
        }

        return info.toString();
    }

    /**
     * Tests if a specific resource can be found using current configuration.
     * 
     * @param resourceName the name of the resource to test
     * @return true if resource is found, false otherwise
     */
    public boolean canFindResource(String resourceName) {
        ClassLoader[] classLoaders = {
                Thread.currentThread().getContextClassLoader(),
                ConfigLoader.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };

        for (ClassLoader classLoader : classLoaders) {
            if (classLoader == null)
                continue;

            for (String prefix : RESOURCE_PREFIXES) {
                String resourcePath = prefix + resourceName;
                try (InputStream is = classLoader.getResourceAsStream(resourcePath)) {
                    if (is != null) {
                        logger.info(
                                "Found resource: " + resourcePath + " using " + classLoader.getClass().getSimpleName());
                        return true;
                    }
                } catch (IOException e) {
                    // Continue searching
                }
            }
        }

        return false;
    }
}
