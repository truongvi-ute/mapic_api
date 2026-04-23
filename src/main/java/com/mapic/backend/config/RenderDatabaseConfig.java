package com.mapic.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration for Render.com database connection
 * Render provides DATABASE_URL in format: postgresql://user:pass@host:port/dbname
 * Spring Boot expects: jdbc:postgresql://host:port/dbname
 * 
 * This config only activates when DATABASE_URL environment variable is present
 */
@Configuration
public class RenderDatabaseConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "DATABASE_URL")
    public DataSource renderDataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        System.out.println("[DATABASE] Detected DATABASE_URL from Render");
        
        // Render format: postgresql://user:pass@host:port/dbname
        // JDBC format: jdbc:postgresql://host:port/dbname
        
        if (databaseUrl.startsWith("postgres://")) {
            databaseUrl = databaseUrl.replace("postgres://", "postgresql://");
        }
        
        if (!databaseUrl.startsWith("jdbc:")) {
            databaseUrl = "jdbc:" + databaseUrl;
        }
        
        System.out.println("[DATABASE] Using Render DATABASE_URL: " + databaseUrl.replaceAll(":[^:@]+@", ":****@"));
        
        return DataSourceBuilder
                .create()
                .url(databaseUrl)
                .build();
    }
}
