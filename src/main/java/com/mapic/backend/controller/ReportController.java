package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.request.ReportRequest;
import com.mapic.backend.service.IReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final IReportService reportService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> submitReport(@RequestBody ReportRequest request) {
        log.info("Request to submit report for {} id {}", request.getTargetType(), request.getTargetId());
        reportService.submitReport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Report submitted successfully", null));
    }
}
