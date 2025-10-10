package com.company.employee.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    
    private static HikariDataSource dataSource;
    
    static {
        try {
            initializeDataSource();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }
    
    private static void initializeDataSource() throws IOException {
        Properties props = new Properties();
        
        // Try to load from properties file, fallback to defaults
        try (InputStream is = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (is != null) {
                props.load(is);
            }
        }
        
        // Set default values if not found in properties file
        String jdbcUrl = props.getProperty("db.url", "jdbc:postgresql://localhost:5432/company_db");
        String username = props.getProperty("db.username", "postgres");
        String password = props.getProperty("db.password", "password");
        String driverClassName = props.getProperty("db.driver", "org.postgresql.Driver");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Connection pool settings
        config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
        config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "2")));
        config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));
        config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "600000")));
        config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.maxLifetime", "1800000")));
        
        // Additional settings
        config.setLeakDetectionThreshold(60000);
        config.setPoolName("EmployeeModulePool");
        
        dataSource = new HikariDataSource(config);
    }
    
    /**
     * Get a database connection from the connection pool
     * @return Database connection
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    /**
     * Close the data source and release all connections
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
    
    /**
     * Get information about the connection pool
     * @return String with pool statistics
     */
    public static String getPoolInfo() {
        if (dataSource != null) {
            return String.format("Pool Info - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                    dataSource.getHikariPoolMXBean().getActiveConnections(),
                    dataSource.getHikariPoolMXBean().getIdleConnections(),
                    dataSource.getHikariPoolMXBean().getTotalConnections(),
                    dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
        return "DataSource not initialized";
    }
}