import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import java.io.File;
import java.io.IOException;

/**
 * JUnit 4 test class for JsonConfigLoader - Java 8 compatible.
 */
public class JsonConfigLoaderTest {

    private JsonConfigLoader loader;
    private String testConfigPath;

    @Before
    public void setUp() {
        testConfigPath = "/Users/sureshselvaraj/myproject/myref/utility/config.json";
        loader = JsonConfigLoader.getInstance(testConfigPath);
    }

    @Test
    public void testGetInstance() {
        assertNotNull("Loader instance should not be null", loader);
        assertTrue("Should be external file", loader.isExternalFile());
        assertEquals("Config file path should match", testConfigPath, loader.getConfigFilePath());
    }

    @Test
    public void testStringRetrieval() {
        String appName = loader.getString("application.name");
        assertNotNull("Application name should not be null", appName);
        assertEquals("Application name should match", "Hybrid EAR Application", appName);

        String defaultValue = loader.getString("non.existent.key", "default");
        assertEquals("Should return default value", "default", defaultValue);
    }

    @Test
    public void testIntegerRetrieval() {
        int minPoolSize = loader.getInt("database.connection.pool.minSize", 0);
        assertEquals("Min pool size should be 5", 5, minPoolSize);

        int defaultValue = loader.getInt("non.existent.key", 42);
        assertEquals("Should return default value", 42, defaultValue);
    }

    @Test
    public void testBooleanRetrieval() {
        boolean cacheEnabled = loader.getBoolean("cache.enabled", false);
        assertTrue("Cache should be enabled", cacheEnabled);

        boolean maintenanceMode = loader.getBoolean("features.maintenanceMode", true);
        assertFalse("Maintenance mode should be false", maintenanceMode);

        boolean defaultValue = loader.getBoolean("non.existent.key", true);
        assertTrue("Should return default value", defaultValue);
    }

    @Test
    public void testLongRetrieval() {
        long timeout = loader.getLong("database.connection.pool.timeout", 0L);
        assertEquals("Timeout should be 30000", 30000L, timeout);

        long defaultValue = loader.getLong("non.existent.key", 123L);
        assertEquals("Should return default value", 123L, defaultValue);
    }

    @Test
    public void testDoubleRetrieval() {
        // Test with version as string that can be parsed as double
        double version = loader.getDouble("application.version", 0.0);
        assertTrue("Version should be greater than 0", version > 0.0);

        double defaultValue = loader.getDouble("non.existent.key", 3.14);
        assertEquals("Should return default value", 3.14, defaultValue, 0.001);
    }

    @Test
    public void testContainsKey() {
        assertTrue("Should contain application.name", loader.containsKey("application.name"));
        assertTrue("Should contain nested key", loader.containsKey("database.connection.pool.minSize"));
        assertFalse("Should not contain non-existent key", loader.containsKey("non.existent.key"));
    }

    @Test
    public void testJsonNodeRetrieval() {
        com.fasterxml.jackson.databind.JsonNode dbConfig = loader.getJsonNode("database");
        assertNotNull("Database config should not be null", dbConfig);
        assertTrue("Database config should be an object", dbConfig.isObject());

        com.fasterxml.jackson.databind.JsonNode poolConfig = loader.getJsonNode("database.connection.pool");
        assertNotNull("Pool config should not be null", poolConfig);
        assertTrue("Pool config should have minSize", poolConfig.has("minSize"));
    }

    @Test
    public void testReload() {
        try {
            loader.reload();
            // If no exception is thrown, reload was successful
            assertTrue("Reload should complete successfully", true);
        } catch (Exception e) {
            fail("Reload should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testInvalidKey() {
        try {
            loader.getString(null);
            fail("Should throw IllegalArgumentException for null key");
        } catch (IllegalArgumentException e) {
            // Expected behavior
            assertTrue("Exception message should mention null key",
                    e.getMessage().contains("null"));
        }

        try {
            loader.getString("");
            fail("Should throw IllegalArgumentException for empty key");
        } catch (IllegalArgumentException e) {
            // Expected behavior
            assertTrue("Exception message should mention empty key",
                    e.getMessage().contains("empty"));
        }
    }

    @Test
    public void testClasspathLoader() {
        JsonConfigLoader classpathLoader = JsonConfigLoader.getInstance();
        assertNotNull("Classpath loader should not be null", classpathLoader);
        assertFalse("Should not be external file", classpathLoader.isExternalFile());
        assertNull("Config file path should be null for classpath", classpathLoader.getConfigFilePath());
    }
}
