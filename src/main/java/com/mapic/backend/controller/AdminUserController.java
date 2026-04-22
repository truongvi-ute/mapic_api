package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.request.UpdateUserStatusRequest;
import com.mapic.backend.dto.response.UserProfileResponse;
import com.mapic.backend.service.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> getUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        
        log.info("[ADMIN-USER] Fetching users - page: {}, size: {}, search: {}, status: {}", 
                 page, size, search, status);
        
        try {
            Page<UserProfileResponse> users = adminUserService.getUsers(page, size, search, status);
            log.info("[ADMIN-USER] Retrieved {} users from {} total", 
                     users.getNumberOfElements(), users.getTotalElements());
            
            return ResponseEntity.ok(
                ApiResponse.success("Users retrieved successfully", users)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-USER] Failed to fetch users: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserById(
            @PathVariable String userId) {
        
        log.info("[ADMIN-USER] Fetching user details for ID: {}", userId);
        
        try {
            UserProfileResponse user = adminUserService.getUserById(userId);
            log.info("[ADMIN-USER] Retrieved user details for: {}", user.getUsername());
            
            return ResponseEntity.ok(
                ApiResponse.success("User details retrieved", user)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-USER] Failed to fetch user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        
        log.info("[ADMIN-USER] Updating status for user {} to {}", userId, request.getStatus());
        
        try {
            adminUserService.updateUserStatus(userId, request);
            log.info("[ADMIN-USER] Status updated successfully for user {}", userId);
            
            return ResponseEntity.ok(
                ApiResponse.success("User status updated successfully", null)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-USER] Failed to update status for user {}: {}", 
                      userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{userId}/activity")
    public ResponseEntity<ApiResponse<Object>> getUserActivity(
            @PathVariable String userId,
            @RequestParam(defaultValue = "7") @Min(1) @Max(90) int days) {
        
        log.info("[ADMIN-USER] Fetching activity for user {} (last {} days)", userId, days);
        
        try {
            Object activity = adminUserService.getUserActivity(userId, days);
            
            return ResponseEntity.ok(
                ApiResponse.success("User activity retrieved", activity)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-USER] Failed to fetch activity for user {}: {}", 
                      userId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<UserProfileResponse>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        
        log.info("[ADMIN-USER] Searching users with query: {}", query);
        
        try {
            Page<UserProfileResponse> users = adminUserService.searchUsers(query, page, size);
            log.info("[ADMIN-USER] Found {} users matching query", users.getTotalElements());
            
            return ResponseEntity.ok(
                ApiResponse.success("Search completed", users)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-USER] Search failed for query {}: {}", query, e.getMessage(), e);
            throw e;
        }
    }
}
