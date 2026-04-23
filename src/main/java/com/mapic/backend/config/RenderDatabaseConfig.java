package com.mapic.backend.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

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
    public DataSource renderDataSource() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");
        
        System.out.println("[DATABASE] Detected DATABASE_URL from Render");
        System.out.println("[DATABASE] Raw URL: " + databaseUrl.replaceAll(":[^:@]+@", ":****@"));
        
        // Parse the DATABASE_URL properly
        URI dbUri = new URI(databaseUrl);
        
        String username = dbUri.getUserInfo().split(":")[0];
        String password = dbUri.getUserInfo().split(":")[1];
        String host = dbUri.getHost();
        int port = dbUri.getPort();
        String database = dbUri.getPath().substring(1); // Remove leading '/'
        
        // Build JDBC URL
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        
        System.out.println("[DATABASE] JDBC URL: " + jdbcUrl);
        System.out.println("[DATABASE] Username: " + username);
        
        return DataSourceBuilder
                .create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("org.postgresql.Driver")
                .build();
    }
}
