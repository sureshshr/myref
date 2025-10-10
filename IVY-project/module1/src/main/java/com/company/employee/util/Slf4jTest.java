package com.company.employee.util;

package com.example.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Slf4jTest {

    static {
        // Determine environment-specific log file path
        String env = System.getenv("APP_ENV"); // e.g., "dev", "test", "prod"
        if (env == null || env.isEmpty())
            env = "dev";

        String logDir = System.getenv("APP_LOG_DIR"); // optional override
        if (logDir == null || logDir.isEmpty())
            logDir = "logs";

        String logFileName = "application-" + env + ".log";
        File logFile = new File(logDir, logFileName);

        // Create log directory if missing
        File parentDir = logFile.getParentFile();
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                System.err.println("⚠️ Failed to create log directory: " + parentDir.getAbsolutePath());
            }
        }

        // Set SLF4J Simple properties before any logger initialization
        System.setProperty("org.slf4j.simpleLogger.logFile", logFile.getAbsolutePath());
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss.SSS");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");

        // Optional: package-specific log levels
        System.setProperty("org.slf4j.simpleLogger.log.com.example.app", "debug");
    }

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

    public static void main(String[] args) {
        logger.info("Application starting in {} environment", System.getenv("APP_ENV"));
        logger.debug("Debug message for tracing application flow");
        logger.warn("Sample warning log");
        logger.error("Sample error log", new RuntimeException("Test exception"));
    }
}
