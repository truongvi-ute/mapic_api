package com.mapic.backend.service;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.AuthResponse;
import com.mapic.backend.dto.LoginRequest;

public interface IAdminAuthService {
    ApiResponse<AuthResponse> login(LoginRequest request);
    ApiResponse<String> logout();
}
