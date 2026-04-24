package com.mapic.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mapic.backend.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Primary
@ConditionalOnBean(Cloudinary.class)
@RequiredArgsConstructor
@Slf4j
public class CloudinaryStorageService implements IStorageService {

    private final Cloudinary cloudinary;

    @Override
    public String store(MultipartFile file, String subDir) {
        if (file.isEmpty()) {
            throw new AppException("Failed to store empty file.");
        }

        try {
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String publicId = "mapic/" + subDir + "/" + UUID.randomUUID().toString();

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "folder", "mapic/" + subDir,
                            "resource_type", "auto",
                            "quality", "auto:good",
                            "fetch_format", "auto"
                    ));

            String secureUrl = (String) uploadResult.get("secure_url");
            log.info("[CLOUDINARY] Uploaded file to: {}", secureUrl);
            
            return secureUrl;
        } catch (IOException e) {
            log.error("[CLOUDINARY] Failed to upload file: {}", e.getMessage());
            throw new AppException("Failed to upload file to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public void delete(String filename, String subDir) {
        try {
            // Extract public_id from URL if it's a Cloudinary URL
            String publicId = extractPublicId(filename, subDir);
            if (publicId != null) {
                Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("[CLOUDINARY] Deleted file with public_id: {} - Result: {}", publicId, result.get("result"));
            }
        } catch (Exception e) {
            log.warn("[CLOUDINARY] Failed to delete file {}: {}", filename, e.getMessage());
        }
    }

    @Override
    public String resolveUrl(String filename, String subDir) {
        if (filename == null || filename.isEmpty()) return null;
        
        // If already a full URL (Cloudinary or other), return as-is
        if (filename.startsWith("http://") || filename.startsWith("https://")) {
            return filename;
        }
        
        // If it's a local filename, we can't resolve it to Cloudinary URL without the public_id
        // This should not happen in normal flow, but return a fallback
        log.warn("[CLOUDINARY] Cannot resolve local filename to Cloudinary URL: {}", filename);
        return "/uploads/" + subDir + "/" + filename;
    }

    @Override
    public boolean exists(String filename, String subDir) {
        if (filename == null || filename.isEmpty()) return false;
        
        // For Cloudinary URLs, assume they exist (can verify with API call if needed)
        if (filename.startsWith("https://res.cloudinary.com/")) {
            return true;
        }
        
        // For other URLs, assume they exist
        if (filename.startsWith("http://") || filename.startsWith("https://")) {
            return true;
        }
        
        // For local filenames, try to check via Cloudinary API
        try {
            String publicId = extractPublicId(filename, subDir);
            if (publicId != null) {
                Map<String, Object> result = cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
                return result != null;
            }
        } catch (Exception e) {
            log.debug("[CLOUDINARY] File existence check failed for {}: {}", filename, e.getMessage());
        }
        
        return false;
    }

    /**
     * Extract public_id from Cloudinary URL or construct it from filename
     */
    private String extractPublicId(String filename, String subDir) {
        if (filename.startsWith("https://res.cloudinary.com/")) {
            // Extract public_id from Cloudinary URL
            // URL format: https://res.cloudinary.com/{cloud_name}/image/upload/v{version}/{public_id}.{format}
            try {
                String[] parts = filename.split("/");
                if (parts.length >= 7) {
                    String publicIdWithExtension = parts[parts.length - 1];
                    String publicId = publicIdWithExtension.contains(".") 
                            ? publicIdWithExtension.substring(0, publicIdWithExtension.lastIndexOf("."))
                            : publicIdWithExtension;
                    
                    // Reconstruct full public_id with folder
                    return "mapic/" + subDir + "/" + publicId;
                }
            } catch (Exception e) {
                log.warn("[CLOUDINARY] Failed to extract public_id from URL: {}", filename);
            }
        } else {
            // Construct public_id from filename
            String filenameWithoutExtension = filename.contains(".") 
                    ? filename.substring(0, filename.lastIndexOf("."))
                    : filename;
            return "mapic/" + subDir + "/" + filenameWithoutExtension;
        }
        
        return null;
    }
}