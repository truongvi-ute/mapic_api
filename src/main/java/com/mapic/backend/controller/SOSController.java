package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.request.TriggerSOSRequest;
import com.mapic.backend.dto.response.SOSAlertDTO;
import com.mapic.backend.dto.response.TriggerSOSResponse;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.SOSService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
public class SOSController {

    private final SOSService sosService;
    private final UserRepository userRepository;

    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<TriggerSOSResponse>> triggerSOS(
            @Valid @RequestBody TriggerSOSRequest request,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        TriggerSOSResponse response = sosService.createSOSAlert(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("SOS Alert triggered successfully", response));
    }

    @PostMapping("/{alertId}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveSOS(
            @PathVariable Long alertId,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        sosService.resolveSOSAlert(alertId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("SOS Alert resolved", null));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<SOSService.ActiveAlertsResponse>> getActiveAlerts(
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        SOSService.ActiveAlertsResponse response = sosService.getActiveAlerts(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Active alerts retrieved", response));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<SOSService.AlertHistoryResponse>> getAlertHistory(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        SOSService.AlertHistoryResponse response = sosService.getAlertHistory(user.getId(), limit, offset);
        return ResponseEntity.ok(ApiResponse.success("Alert history retrieved", response));
    }

    @PostMapping("/{alertId}/view")
    public ResponseEntity<ApiResponse<Void>> markAsViewed(
            @PathVariable Long alertId,
            Authentication authentication) {
        
        User user = getCurrentUser(authentication);
        sosService.markAlertAsViewed(alertId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Alert marked as viewed", null));
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
