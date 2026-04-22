package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.request.CreateReportRequest;
import com.mapic.backend.dto.response.ReportResponse;
import com.mapic.backend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {
    
    private final ReportService reportService;
    
    @PostMapping("/moments/{momentId}")
    public ResponseEntity<ApiResponse<Void>> reportMoment(
            @PathVariable Long momentId,
            @Valid @RequestBody CreateReportRequest request) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("[REPORT] User {} reporting moment {}", username, momentId);
        
        reportService.reportMoment(username, momentId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Report submitted successfully", null)
        );
    }
    
    @PostMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> reportComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CreateReportRequest request) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("[REPORT] User {} reporting comment {}", username, commentId);
        
        reportService.reportComment(username, commentId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Report submitted successfully", null)
        );
    }
    
    @PostMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> reportUser(
            @PathVariable Long userId,
            @Valid @RequestBody CreateReportRequest request) {
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("[REPORT] User {} reporting user {}", username, userId);
        
        reportService.reportUser(username, userId, request);
        
        return ResponseEntity.ok(
            ApiResponse.success("Report submitted successfully", null)
        );
    }
    
    @GetMapping("/my-reports")
    public ResponseEntity<ApiResponse<List<ReportResponse>>> getMyReports() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("[REPORT] User {} fetching their reports", username);
        
        List<ReportResponse> reports = reportService.getMyReports(username);
        
        return ResponseEntity.ok(
            ApiResponse.success("Reports retrieved", reports)
        );
    }
}
