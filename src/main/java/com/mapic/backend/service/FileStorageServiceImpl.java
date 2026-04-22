package com.mapic.backend.service;

import com.mapic.backend.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements IStorageService {

    private final Path rootLocation;

    public FileStorageServiceImpl(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new AppException("Could not initialize storage location");
        }
    }

    @Override
    public String store(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new AppException("Failed to store empty file.");
        }

        try {
            Path targetDir = this.rootLocation.resolve(subDir);
            Files.createDirectories(targetDir);

            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String extension = originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetDir.resolve(filename),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            
            return filename;
        } catch (IOException e) {
            throw new AppException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public void delete(String filename, String subDir) {
        try {
            Path file = this.rootLocation.resolve(subDir).resolve(filename);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("Failed to delete file {}: {}", filename, e.getMessage());
        }
    }

    @Override
    public String resolveUrl(String filename, String subDir) {
        if (filename == null || filename.isEmpty()) return null;
        
        // If already a full URL (http:// or https://), return as-is
        if (filename.startsWith("http://") || filename.startsWith("https://")) {
            return filename;
        }
        
        // If already starts with /uploads/, return as-is (already resolved)
        if (filename.startsWith("/uploads/")) {
            return filename;
        }
        
        // Otherwise, build the URL path
        return "/uploads/" + subDir + "/" + filename;
    }
}
