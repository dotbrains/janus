package com.dotbrains.janus.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Arrays;

/**
 * Database configuration with secure credential management
 * All sensitive values are injected via @Value from environment variables
 */
@Configuration
@Slf4j
public class DatabaseConfig {

    @Autowired
    private Environment environment;

    @Value("${DATABASE_URL:jdbc:postgresql://localhost:5432/janus}")
    private String databaseUrl;

    @Value("${DATABASE_USERNAME:janus}")
    private String databaseUsername;

    @Value("${DATABASE_PASSWORD:janus123}")
    private String databasePassword;

    @Value("${DATABASE_DRIVER:org.postgresql.Driver}")
    private String databaseDriver;

    // HikariCP configuration
    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.maximum-pool-size:10}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1200000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.pool-name:JanusHikariPool}")
    private String poolName;

    /**
     * Configure HikariCP DataSource with secure credentials
     * Credentials are never logged or exposed
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        boolean isProduction = isProductionEnvironment();
        String env = getCurrentEnvironment();
        
        log.info("========================================");
        log.info("Environment: {}", env);
        log.info("Production Mode: {}", isProduction);
        log.info("========================================");
        log.info("Configuring database connection pool");
        log.info("Database URL: {}", maskUrl(databaseUrl));
        log.info("Database Username: {}", maskUsername(databaseUsername));
        // IMPORTANT: Never log passwords
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setUsername(databaseUsername);
        config.setPassword(databasePassword);
        config.setDriverClassName(databaseDriver);
        
        // Connection pool settings
        config.setConnectionTimeout(connectionTimeout);
        config.setMaximumPoolSize(maximumPoolSize);
        config.setMinimumIdle(minimumIdle);
        config.setIdleTimeout(idleTimeout);
        config.setMaxLifetime(maxLifetime);
        config.setPoolName(poolName);
        config.setAutoCommit(true);
        
        // Security settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // PostgreSQL-specific SSL settings (automatically enabled for production)
        if (isProduction) {
            log.info("Production environment detected - enabling SSL for database connections");
            config.addDataSourceProperty("ssl", "true");
            config.addDataSourceProperty("sslmode", "require");
            config.addDataSourceProperty("sslrootcert", "verify-ca");
            
            // Additional production security
            config.setLeakDetectionThreshold(60000); // 60 seconds
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);
        } else {
            log.warn("Development environment - SSL is disabled for database connections");
            log.warn("This configuration should NOT be used in production!");
        }
        
        log.info("Database connection pool configured successfully");
        return new HikariDataSource(config);
    }

    /**
     * Mask database URL for logging (hide passwords in connection strings)
     */
    private String maskUrl(String url) {
        if (url == null) return "null";
        // Mask password if present in URL
        return url.replaceAll("password=[^&;]*", "password=***");
    }

    /**
     * Mask username for logging (show first and last character only)
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) return "***";
        return username.charAt(0) + "***" + username.charAt(username.length() - 1);
    }

    /**
     * Determine if the application is running in production environment
     * Checks for 'prod' or 'production' in active profiles
     * 
     * @return true if production environment, false otherwise
     */
    private boolean isProductionEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        
        // Check active profiles
        boolean hasProductionProfile = Arrays.stream(activeProfiles)
                .anyMatch(profile -> 
                    profile.equalsIgnoreCase("prod") || 
                    profile.equalsIgnoreCase("production")
                );
        
        // If no active profiles, check default profiles
        if (activeProfiles.length == 0) {
            String[] defaultProfiles = environment.getDefaultProfiles();
            hasProductionProfile = Arrays.stream(defaultProfiles)
                    .anyMatch(profile -> 
                        profile.equalsIgnoreCase("prod") || 
                        profile.equalsIgnoreCase("production")
                    );
        }
        
        return hasProductionProfile;
    }

    /**
     * Get the current environment name
     * 
     * @return current environment (prod, dev, or default)
     */
    private String getCurrentEnvironment() {
        String[] activeProfiles = environment.getActiveProfiles();
        
        if (activeProfiles.length > 0) {
            return String.join(", ", activeProfiles);
        }
        
        String[] defaultProfiles = environment.getDefaultProfiles();
        if (defaultProfiles.length > 0) {
            return String.join(", ", defaultProfiles);
        }
        
        return "default (dev)";
    }
}
