package com.mapic.backend.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration for Render.com database connection
 * Render provides DATABASE_URL in format: postgresql://user:pass@host:port/dbname
 * Spring Boot expects: jdbc:postgresql://host:port/dbname
 */
@Configuration
public class RenderDatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        // If DATABASE_URL exists (Render environment), use it
        if (databaseUrl != null && !databaseUrl.isEmpty()) {
            // Render format: postgresql://user:pass@host:port/dbname
            // JDBC format: jdbc:postgresql://host:port/dbname
            
            if (databaseUrl.startsWith("postgres://")) {
                databaseUrl = databaseUrl.replace("postgres://", "postgresql://");
            }
            
            if (!databaseUrl.startsWith("jdbc:")) {
                databaseUrl = "jdbc:" + databaseUrl;
            }
            
            System.out.println("[DATABASE] Using Render DATABASE_URL");
            
            return DataSourceBuilder
                    .create()
                    .url(databaseUrl)
                    .build();
        }
        
        // Otherwise, use default Spring Boot configuration from application.properties
        System.out.println("[DATABASE] Using local configuration from application.properties");
        return DataSourceBuilder
                .create()
                .build();
    }
}
