/**
 * Simple example demonstrating the singleton ConfigLoader usage.
 * Java 8 compatible.
 */
public class ConfigLoaderExample {

    public static void main(String[] args) {
        System.out.println("=== ConfigLoader Singleton Demo ===\n");

        try {
            // Get singleton instance
            ConfigLoader config = ConfigLoader.getInstance();

            // Demonstrate that it's the same instance
            ConfigLoader config2 = ConfigLoader.getInstance();
            System.out.println("Same instance: " + (config == config2));

            // Basic property retrieval with defaults
            String dbUrl = config.get("db.url", "jdbc:mysql://localhost:3306/defaultdb");
            String appName = config.get("app.name", "My Application");

            System.out.println("Database URL: " + dbUrl);
            System.out.println("Application Name: " + appName);

            // Type-specific getters
            int serverPort = config.getInt("server.port", 8080);
            boolean debugMode = config.getBoolean("app.debug", false);
            long cacheSize = config.getLong("cache.size", 1024L);
            double version = config.getDouble("app.version", 1.0);

            System.out.println("Server Port: " + serverPort);
            System.out.println("Debug Mode: " + debugMode);
            System.out.println("Cache Size: " + cacheSize);
            System.out.println("Application Version: " + version);

            // Check if properties exist
            System.out.println("Has 'db.url': " + config.containsKey("db.url"));
            System.out.println("Has 'missing.key': " + config.containsKey("missing.key"));

            // Reload configuration
            System.out.println("\nReloading configuration...");
            config.reload();
            System.out.println("Configuration reloaded successfully");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
