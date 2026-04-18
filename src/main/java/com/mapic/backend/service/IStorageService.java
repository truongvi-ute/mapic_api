package com.mapic.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface IStorageService {
    /**
     * Stores a file and returns its stored filename/path.
     */
    String store(MultipartFile file, String subDir);
    
    /**
     * Deletes a file from storage.
     */
    void delete(String filename, String subDir);
    
    /**
     * Builds a full URL or path for a given filename.
     */
    String resolveUrl(String filename, String subDir);
}
