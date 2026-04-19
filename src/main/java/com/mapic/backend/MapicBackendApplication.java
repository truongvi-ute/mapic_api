package com.mapic.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MapicBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MapicBackendApplication.class, args);
	}

}
