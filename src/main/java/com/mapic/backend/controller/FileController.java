package com.mapic.backend.controller;

import com.mapic.backend.service.IStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final IStorageService storageService;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/{subDir}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @PathVariable String subDir,
            @PathVariable String filename) {
        
        try {
            // Check if file exists
            if (!storageService.exists(filename, subDir)) {
                log.warn("File not found: {}/{}", subDir, filename);
                return ResponseEntity.notFound().build();
            }
            
            Path filePath = Paths.get(uploadDir).resolve(subDir).resolve(filename);
            Resource resource = new FileSystemResource(filePath);
            
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("File not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(resource);
                    
        } catch (IOException e) {
            log.error("Error serving file {}/{}: {}", subDir, filename, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}