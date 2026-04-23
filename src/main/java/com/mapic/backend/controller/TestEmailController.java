package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.service.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Test Controller for Email Service
 * Use this to test email sending functionality
 * 
 * ⚠️ REMOVE THIS CONTROLLER IN PRODUCTION
 */
@RestController
@RequestMapping("/api/test/email")
@RequiredArgsConstructor
@Slf4j
public class TestEmailController {

    private final IEmailService emailService;

    /**
     * Test sending OTP email
     * GET /api/test/email/otp?email=test@example.com
     */
    @GetMapping("/otp")
    public ResponseEntity<ApiResponse<String>> testSendOtp(
            @RequestParam String email,
            @RequestParam(defaultValue = "Test User") String name
    ) {
        log.info("Testing OTP email to: {}", email);
        
        try {
            String otp = generateTestOtp();
            emailService.sendOtpEmail(email, otp, name);
            
            return ResponseEntity.ok(ApiResponse.success(
                "OTP email sent successfully! Check your inbox.",
                "OTP: " + otp
            ));
        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
        }
    }

    /**
     * Test sending password reset OTP
     * GET /api/test/email/reset-password?email=test@example.com
     */
    @GetMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> testSendPasswordResetOtp(
            @RequestParam String email,
            @RequestParam(defaultValue = "Test User") String name
    ) {
        log.info("Testing password reset OTP email to: {}", email);
        
        try {
            String otp = generateTestOtp();
            emailService.sendPasswordResetOtp(email, otp, name);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Password reset OTP email sent successfully!",
                "OTP: " + otp
            ));
        } catch (Exception e) {
            log.error("Failed to send password reset OTP email", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
        }
    }

    /**
     * Test sending welcome email
     * GET /api/test/email/welcome?email=test@example.com
     */
    @GetMapping("/welcome")
    public ResponseEntity<ApiResponse<String>> testSendWelcome(
            @RequestParam String email,
            @RequestParam(defaultValue = "Test User") String name
    ) {
        log.info("Testing welcome email to: {}", email);
        
        try {
            emailService.sendWelcomeEmail(email, name);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Welcome email sent successfully!",
                null
            ));
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
        }
    }

    /**
     * Test sending custom email
     * POST /api/test/email/custom
     */
    @PostMapping("/custom")
    public ResponseEntity<ApiResponse<String>> testSendCustom(
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String body
    ) {
        log.info("Testing custom email to: {}", email);
        
        try {
            emailService.sendEmail(email, subject, body);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Custom email sent successfully!",
                null
            ));
        } catch (Exception e) {
            log.error("Failed to send custom email", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("Failed to send email: " + e.getMessage()));
        }
    }

    /**
     * Generate a random 6-digit OTP for testing
     */
    private String generateTestOtp() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
}
