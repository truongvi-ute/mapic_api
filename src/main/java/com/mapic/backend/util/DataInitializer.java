package com.mapic.backend.util;

import com.mapic.backend.service.ILocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ILocationService locationService;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking if location data needs to be seeded...");
        if (locationService.getAllProvinces().isEmpty()) {
            locationService.seedLocations();
        } else {
            log.info("Location data already exists. Skipping seeding.");
        }
    }
}
