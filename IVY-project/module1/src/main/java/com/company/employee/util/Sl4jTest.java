package com.company.employee.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sl4jTest {

    static {
        // Set log level
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");

        // Show timestamps
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss.SSS");

        // Set output file path dynamically (e.g., from environment variable)
        String logFile = System.getenv("APP_LOG_FILE");
        if (logFile == null || logFile.isEmpty()) {
            logFile = "/opt/app/logs/default.log";
        }
        System.setProperty("org.slf4j.simpleLogger.logFile", logFile);
    }

    private static final Logger logger = LoggerFactory.getLogger(SimpleLoggerConfigExample.class);

    public static void main(String[] args) {
        logger.info("Application started successfully");
        logger.warn("Running in production mode");
        logger.error("Simulated error", new RuntimeException("test"));
    }
}
