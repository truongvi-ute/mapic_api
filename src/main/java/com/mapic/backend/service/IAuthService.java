package com.mapic.backend.service;

import com.mapic.backend.dto.*;

public interface IAuthService {
    ApiResponse<String> register(RegisterRequest request);
    ApiResponse<AuthResponse> verifyRegistration(VerifyOtpRequest request);
    ApiResponse<String> resendOtp(ResendOtpRequest request);
    ApiResponse<AuthResponse> login(LoginRequest request);
    ApiResponse<String> forgotPassword(ForgotPasswordRequest request);
    ApiResponse<String> resetPassword(ResetPasswordRequest request);
    ApiResponse<String> changePassword(ForgotPasswordRequest request);
    ApiResponse<AuthResponse> verifyToken(String token);
}
