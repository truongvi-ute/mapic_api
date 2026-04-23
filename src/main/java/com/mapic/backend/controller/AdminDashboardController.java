package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.entity.ReportStatus;
import com.mapic.backend.repository.MomentRepository;
import com.mapic.backend.repository.ReportRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final ReportRepository reportRepository;

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardMetrics() {
        log.info("[DASHBOARD] Getting dashboard metrics");
        
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // Real data from database
            long totalUsers = userRepository.count();
            long totalMoments = momentRepository.count();
            long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
            long resolvedReports = reportRepository.countByStatus(ReportStatus.RESOLVED);
            long totalReports = reportRepository.count();
            
            // Today's data (from midnight)
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            long momentsToday = momentRepository.countByCreatedAtAfter(startOfDay);
            
            // Active users (users with status ACTIVE)
            long activeUsers = userRepository.countByStatus(com.mapic.backend.entity.AccountStatus.ACTIVE);
            
            metrics.put("totalUsers", (int) totalUsers);
            metrics.put("activeUsers", (int) activeUsers);
            metrics.put("totalMoments", (int) totalMoments);
            metrics.put("momentsToday", (int) momentsToday);
            metrics.put("pendingReports", (int) pendingReports);
            metrics.put("resolvedReports", (int) resolvedReports);
            metrics.put("resolvedReportsToday", 0); // TODO: Add createdAt filter
            metrics.put("totalReports", (int) totalReports);
            
            log.info("[DASHBOARD] Metrics: users={}, moments={}, reports={}", 
                     totalUsers, totalMoments, totalReports);
            
            return ResponseEntity.ok(ApiResponse.success("Dashboard metrics retrieved", metrics));
            
        } catch (Exception e) {
            log.error("[DASHBOARD] Error getting metrics: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/trends")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardTrends() {
        log.info("[DASHBOARD] Getting dashboard trends");
        
        Map<String, Object> trends = new HashMap<>();
        
        // Mock trend data for now
        trends.put("userGrowth", new int[]{0, 1, 2, 3, 4, 5});
        trends.put("momentGrowth", new int[]{0, 3, 6, 9, 12, 15});
        trends.put("reportTrends", new int[]{0, 0, 0, 0, 0, 0});
        
        return ResponseEntity.ok(ApiResponse.success("Dashboard trends retrieved", trends));
    }
}