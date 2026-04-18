package com.mapic.backend.service;

import com.mapic.backend.dto.RegisterRequest;
import com.mapic.backend.entity.OtpType;
import java.util.Optional;

public interface IOtpService {
    String generateAndStoreOtp(String email, OtpType type);
    boolean validateOtp(String email, String code, OtpType type);
    void deleteOtp(String email, OtpType type);
    
    void savePendingRegistration(String email, RegisterRequest data);
    Optional<RegisterRequest> getPendingRegistration(String email);
    void deletePendingRegistration(String email);
}
