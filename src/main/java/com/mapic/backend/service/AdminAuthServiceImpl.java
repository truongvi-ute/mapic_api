package com.mapic.backend.service;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.AuthResponse;
import com.mapic.backend.dto.LoginRequest;
import com.mapic.backend.entity.AccountStatus;
import com.mapic.backend.entity.Admin;
import com.mapic.backend.exception.AppException;
import com.mapic.backend.repository.AdminRepository;
import com.mapic.backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuthServiceImpl implements IAdminAuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public ApiResponse<AuthResponse> login(LoginRequest request) {
        log.info("Admin login attempt: {}", request.getUsername());
        
        // Find admin by username or email
        Admin admin = adminRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new AppException("Invalid username/email or password"));

        // Check if admin is active
        if (!admin.getIsActive()) {
            throw new AppException("Admin account is deactivated. Please contact super admin.");
        }

        // Check account status
        if (admin.getStatus() == AccountStatus.BLOCK) {
            throw new AppException("Admin account is blocked. Please contact super admin.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new AppException("Invalid username/email or password");
        }

        // Generate JWT token with admin role
        String token = jwtUtil.generateToken(admin.getUsername(), admin.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .build();

        log.info("Admin login successful: {} (role: {})", admin.getUsername(), admin.getRole());
        return ApiResponse.success("Admin login successful", response);
    }

    @Override
    public ApiResponse<String> logout() {
        // In stateless JWT, logout is handled client-side by removing token
        // Optionally, you can implement token blacklist here
        return ApiResponse.success("Admin logout successful", null);
    }
}
