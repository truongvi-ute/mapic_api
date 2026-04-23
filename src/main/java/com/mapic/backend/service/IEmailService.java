package com.mapic.backend.service;

/**
 * Email Service Interface
 * Provides methods for sending various types of emails
 */
public interface IEmailService {
    
    /**
     * Send OTP email for registration verification
     * @param toEmail recipient email address
     * @param otp the OTP code
     * @param userName recipient's name
     */
    void sendOtpEmail(String toEmail, String otp, String userName);
    
    /**
     * Send password reset OTP email
     * @param toEmail recipient email address
     * @param otp the OTP code
     * @param userName recipient's name
     */
    void sendPasswordResetOtp(String toEmail, String otp, String userName);
    
    /**
     * Send welcome email after successful registration
     * @param toEmail recipient email address
     * @param userName recipient's name
     */
    void sendWelcomeEmail(String toEmail, String userName);
    
    /**
     * Send generic email
     * @param toEmail recipient email address
     * @param subject email subject
     * @param body email body (HTML supported)
     */
    void sendEmail(String toEmail, String subject, String body);
}
