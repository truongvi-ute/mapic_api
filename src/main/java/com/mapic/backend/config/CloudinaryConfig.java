package com.mapic.backend.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    @ConditionalOnProperty(name = {"cloudinary.cloud-name", "cloudinary.api-key", "cloudinary.api-secret"})
    public Cloudinary cloudinary() {
        System.out.println("[CLOUDINARY] ========== Cloudinary Configuration ==========");
        System.out.println("[CLOUDINARY] Cloud Name: " + (cloudName != null && !cloudName.isEmpty() ? cloudName : "NOT_SET"));
        System.out.println("[CLOUDINARY] API Key: " + (apiKey != null && !apiKey.isEmpty() ? "SET" : "NOT_SET"));
        System.out.println("[CLOUDINARY] API Secret: " + (apiSecret != null && !apiSecret.isEmpty() ? "SET" : "NOT_SET"));
        System.out.println("[CLOUDINARY] =============================================");

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }
}