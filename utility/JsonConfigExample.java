import java.util.Date;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Example usage of JsonConfigLoader for loading configuration from external
 * paths.
 * Java 8 compatible version.
 */
public class JsonConfigExample {

    public static void main(String[] args) {
        // Example 1: Load from classpath (default)
        System.out.println("=== Loading from Classpath ===");
        JsonConfigLoader classpathLoader = JsonConfigLoader.getInstance();
        demonstrateConfigLoader(classpathLoader);

        // Example 2: Load from external file path
        System.out.println("\n=== Loading from External File ===");
        String externalPath = "/Users/sureshselvaraj/myproject/myref/utility/config.json";
        JsonConfigLoader externalLoader = JsonConfigLoader.getInstance(externalPath);
        demonstrateConfigLoader(externalLoader);

        // Example 3: Configuration with different data types
        System.out.println("\n=== Configuration Data Types ===");
        demonstrateDataTypes(externalLoader);
    }

    private static void demonstrateConfigLoader(JsonConfigLoader loader) {
        try {
            System.out.println("Config file path: " + loader.getConfigFilePath());
            System.out.println("Is external file: " + loader.isExternalFile());
            System.out.println("Last modified: " + new Date(loader.getLastModified()));

            // Basic string retrieval
            String appName = loader.getString("application.name", "Unknown App");
            System.out.println("Application Name: " + appName);

            // Nested property access
            String dbUrl = loader.getString("database.url");
            System.out.println("Database URL: " + dbUrl);

            // Check if key exists
            boolean hasJwtConfig = loader.containsKey("security.jwt");
            System.out.println("Has JWT config: " + hasJwtConfig);

        } catch (Exception e) {
            System.err.println("Error loading configuration: " + e.getMessage());
        }
    }

    private static void demonstrateDataTypes(JsonConfigLoader loader) {
        try {
            // String values
            String environment = loader.getString("application.environment", "production");
            System.out.println("Environment: " + environment);

            // Integer values
            int minPoolSize = loader.getInt("database.connection.pool.minSize", 5);
            int maxPoolSize = loader.getInt("database.connection.pool.maxSize", 20);
            System.out.println("Pool Size: " + minPoolSize + " - " + maxPoolSize);

            // Boolean values
            boolean cacheEnabled = loader.getBoolean("cache.enabled", false);
            boolean maintenanceMode = loader.getBoolean("features.maintenanceMode", false);
            System.out.println("Cache Enabled: " + cacheEnabled);
            System.out.println("Maintenance Mode: " + maintenanceMode);

            // Long values
            long timeout = loader.getLong("database.connection.pool.timeout", 30000L);
            System.out.println("Connection Timeout: " + timeout + "ms");

            // Double values
            double version = loader.getDouble("application.version", 1.0);
            System.out.println("Application Version: " + version);

            // Complex JSON objects
            JsonNode smtpConfig = loader.getJsonNode("email.smtp");
            if (smtpConfig != null) {
                System.out.println("SMTP Host: " + smtpConfig.get("host").asText());
                System.out.println("SMTP Port: " + smtpConfig.get("port").asInt());
                System.out.println("SMTP StartTLS: " + smtpConfig.get("startTls").asBoolean());
            }

            // Reload configuration
            System.out.println("\nReloading configuration...");
            loader.reload();
            System.out.println("Configuration reloaded successfully");

        } catch (Exception e) {
            System.err.println("Error demonstrating data types: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
