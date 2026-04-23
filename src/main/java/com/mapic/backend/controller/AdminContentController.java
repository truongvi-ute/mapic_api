package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.request.ModerationActionRequest;
import com.mapic.backend.dto.response.ContentAnalyticsResponse;
import com.mapic.backend.dto.response.ReportResponse;
import com.mapic.backend.service.AdminContentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
@Slf4j
public class AdminContentController {

    private final AdminContentService adminContentService;

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<Page<ReportResponse>>> getReportQueue(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        log.info("[ADMIN-CONTENT] Fetching report queue - page: {}, size: {}, type: {}, status: {}", 
                 page, size, type, status);
        
        try {
            Page<ReportResponse> reports = adminContentService.getReportQueue(
                page, size, type, status, search);
            log.info("[ADMIN-CONTENT] Retrieved {} reports from {} total", 
                     reports.getNumberOfElements(), reports.getTotalElements());
            
            return ResponseEntity.ok(
                ApiResponse.success("Report queue retrieved", reports)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-CONTENT] Failed to fetch report queue: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/reports/{reportId}/moderate")
    public ResponseEntity<ApiResponse<Void>> moderateContent(
            @PathVariable String reportId,
            @Valid @RequestBody ModerationActionRequest request) {
        
        log.info("[ADMIN-CONTENT] ========== MODERATION REQUEST START ==========");
        log.info("[ADMIN-CONTENT] Report ID: {}", reportId);
        log.info("[ADMIN-CONTENT] Action: {}", request.getAction());
        log.info("[ADMIN-CONTENT] Reason: {}", request.getReason());
        log.info("[ADMIN-CONTENT] Note: {}", request.getNote());
        log.info("[ADMIN-CONTENT] Notify User: {}", request.isNotifyUser());
        log.info("[ADMIN-CONTENT] Notify Reporter: {}", request.isNotifyReporter());
        
        try {
            adminContentService.moderateContent(reportId, request);
            log.info("[ADMIN-CONTENT] Moderation action completed successfully for report {}", reportId);
            log.info("[ADMIN-CONTENT] ========== MODERATION REQUEST END (SUCCESS) ==========");
            
            return ResponseEntity.ok(
                ApiResponse.success("Content moderated successfully", null)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-CONTENT] ========== MODERATION REQUEST END (FAILED) ==========");
            log.error("[ADMIN-CONTENT] Failed to moderate report {}: {}", 
                      reportId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<ContentAnalyticsResponse>> getContentAnalytics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String type) {
        
        log.info("[ADMIN-CONTENT] Fetching content analytics - period: {}, type: {}", 
                 period, type);
        
        try {
            ContentAnalyticsResponse analytics = adminContentService.getContentAnalytics(
                period, type);
            log.info("[ADMIN-CONTENT] Content analytics retrieved successfully");
            
            return ResponseEntity.ok(
                ApiResponse.success("Content analytics retrieved", analytics)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-CONTENT] Failed to fetch content analytics: {}", 
                      e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReportDetails(
            @PathVariable String reportId) {
        
        log.info("[ADMIN-CONTENT] Fetching report details for ID: {}", reportId);
        
        try {
            ReportResponse report = adminContentService.getReportDetails(reportId);
            log.info("[ADMIN-CONTENT] Report details retrieved for: {}", reportId);
            
            return ResponseEntity.ok(
                ApiResponse.success("Report details retrieved", report)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-CONTENT] Failed to fetch report {}: {}", 
                      reportId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/reports/{reportId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveContent(@PathVariable String reportId) {
        
        log.info("[ADMIN-CONTENT] Approving content for report {}", reportId);
        
        try {
            adminContentService.approveContent(reportId);
            log.info("[ADMIN-CONTENT] Content approved for report {}", reportId);
            
            return ResponseEntity.ok(
                ApiResponse.success("Content approved successfully", null)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-CONTENT] Failed to approve content for report {}: {}", 
                      reportId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/reports/{reportId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectContent(
            @PathVariable String reportId,
            @RequestParam(required = false) String reason) {
        
        log.info("[ADMIN-CONTENT] Rejecting content for report {} with reason: {}", 
                 reportId, reason);
        
        try {
            adminContentService.rejectContent(reportId, reason);
            log.info("[ADMIN-CONTENT] Content rejected for report {}", reportId);
            
            return ResponseEntity.ok(
                ApiResponse.success("Content rejected successfully", null)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-CONTENT] Failed to reject content for report {}: {}", 
                      reportId, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getContentStatistics() {
        
        log.info("[ADMIN-CONTENT] Fetching content statistics");
        
        try {
            Object statistics = adminContentService.getContentStatistics();
            
            return ResponseEntity.ok(
                ApiResponse.success("Content statistics retrieved", statistics)
            );
            
        } catch (Exception e) {
            log.error("[ADMIN-CONTENT] Failed to fetch content statistics: {}", 
                      e.getMessage(), e);
            throw e;
        }
    }
}
