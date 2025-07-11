# JSON Configuration Loader - Java 8 Compatible

A thread-safe, flexible JSON configuration loader that supports loading from both classpath and external file paths. This utility is fully compatible with Java 8 and provides robust configuration management capabilities.

## Features

- **Java 8 Compatible**: Designed specifically for Java 8 environments
- **Flexible Loading**: Load from classpath or external file paths
- **Thread-Safe**: Uses double-checked locking pattern for thread safety
- **Automatic Reload**: Monitors external files for changes and reloads automatically
- **Type Support**: Built-in support for String, int, long, double, boolean, and JsonNode types
- **Dot Notation**: Access nested properties using dot notation (e.g., `database.connection.pool.maxSize`)
- **Default Values**: Fallback support for missing configuration keys
- **Comprehensive Error Handling**: Graceful handling of missing files and invalid values

## Requirements

- Java 8 or higher
- Jackson Databind 2.15.3 (Java 8 compatible version)
- Maven 3.6+ (for building)

## Installation

### Maven Dependency

Add the following dependencies to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-core</artifactId>
        <version>2.15.3</version>
    </dependency>

    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.3</version>
    </dependency>
</dependencies>
```

### Java 8 Compiler Configuration

Ensure your `pom.xml` has Java 8 compatibility:

```xml
<properties>
    <maven.compiler.source>1.8</maven.compiler.source>
    <maven.compiler.target>1.8</maven.compiler.target>
</properties>
```

## Usage

### Basic Usage

```java
// Load from classpath (config.json in resources)
JsonConfigLoader loader = JsonConfigLoader.getInstance();

// Load from external file path
JsonConfigLoader loader = JsonConfigLoader.getInstance("/path/to/config.json");

// Get configuration values
String dbUrl = loader.getString("database.url");
int poolSize = loader.getInt("database.connection.pool.maxSize", 10);
boolean cacheEnabled = loader.getBoolean("cache.enabled", false);
```

### Configuration File Format

Create a `config.json` file with your configuration:

```json
{
  "application": {
    "name": "My Application",
    "version": "1.0.0",
    "environment": "development"
  },
  "database": {
    "url": "jdbc:sqlserver://localhost:1433;databaseName=MyDB",
    "username": "user",
    "password": "password",
    "connection": {
      "pool": {
        "minSize": 5,
        "maxSize": 20,
        "timeout": 30000
      }
    }
  },
  "features": {
    "enableAudit": true,
    "maintenanceMode": false
  }
}
```

### Supported Data Types

```java
// String values
String appName = loader.getString("application.name");
String appName = loader.getString("application.name", "Default App");

// Integer values
int maxPoolSize = loader.getInt("database.connection.pool.maxSize", 20);

// Boolean values
boolean auditEnabled = loader.getBoolean("features.enableAudit", false);

// Long values
long timeout = loader.getLong("database.connection.pool.timeout", 30000L);

// Double values
double version = loader.getDouble("application.version", 1.0);

// Complex JSON objects
JsonNode poolConfig = loader.getJsonNode("database.connection.pool");
if (poolConfig != null) {
    int minSize = poolConfig.get("minSize").asInt();
    int maxSize = poolConfig.get("maxSize").asInt();
}
```

### Utility Methods

```java
// Check if a key exists
boolean hasJwtConfig = loader.containsKey("security.jwt");

// Reload configuration
loader.reload();

// Get configuration information
String configPath = loader.getConfigFilePath();
boolean isExternal = loader.isExternalFile();
long lastModified = loader.getLastModified();
```

## Example Application

```java
public class ConfigExample {
    public static void main(String[] args) {
        // Load configuration from external file
        JsonConfigLoader config = JsonConfigLoader.getInstance("/app/config.json");

        // Application settings
        String appName = config.getString("application.name", "Unknown App");
        String environment = config.getString("application.environment", "production");

        // Database configuration
        String dbUrl = config.getString("database.url");
        String dbUser = config.getString("database.username");
        int minPoolSize = config.getInt("database.connection.pool.minSize", 5);
        int maxPoolSize = config.getInt("database.connection.pool.maxSize", 20);

        // Feature flags
        boolean auditEnabled = config.getBoolean("features.enableAudit", false);
        boolean maintenanceMode = config.getBoolean("features.maintenanceMode", false);

        System.out.println("Application: " + appName + " (" + environment + ")");
        System.out.println("Database: " + dbUrl);
        System.out.println("Pool Size: " + minPoolSize + "-" + maxPoolSize);
        System.out.println("Audit: " + auditEnabled + ", Maintenance: " + maintenanceMode);
    }
}
```

## Building

To build the project:

```bash
mvn clean compile
```

To run tests:

```bash
mvn test
```

To create a JAR file:

```bash
mvn package
```

## Key Differences from Modern Java Versions

This Java 8 compatible version:

- Uses `java.io.File` instead of modern `java.nio.file.Path` APIs
- Uses traditional try-with-resources (available since Java 7)
- Uses `java.util.Date` instead of modern time APIs
- Compatible with Jackson 2.15.3 (last version supporting Java 8)
- Uses JUnit 4 instead of JUnit 5 for broader compatibility

## Thread Safety

The `JsonConfigLoader` uses the singleton pattern with double-checked locking to ensure thread safety. The configuration is loaded once and cached, with automatic reloading when external files are modified.

## Error Handling

- Missing configuration files result in empty configuration (no errors thrown)
- Invalid property values return default values with warning logs
- Null or empty keys throw `IllegalArgumentException`
- File I/O errors are logged and wrapped in `RuntimeException`

## License

This utility is provided as-is for educational and development purposes.
