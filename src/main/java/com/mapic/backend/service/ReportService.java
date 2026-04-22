package com.mapic.backend.service;

import com.mapic.backend.dto.request.CreateReportRequest;
import com.mapic.backend.dto.response.ReportResponse;
import com.mapic.backend.entity.*;
import com.mapic.backend.exception.AppException;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.repository.CommentRepository;
import com.mapic.backend.repository.MomentRepository;
import com.mapic.backend.repository.ReportRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    
    @Transactional
    public void reportMoment(String username, Long momentId, CreateReportRequest request) {
        User reporter = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        Moment moment = momentRepository.findById(momentId)
            .orElseThrow(() -> new NotFoundException("Moment not found"));
        
        // Check if already reported
        boolean exists = reportRepository.existsByReporterIdAndTargetIdAndTargetType(
            reporter.getId(), momentId, ReportTargetType.MOMENT);
        
        if (exists) {
            throw new AppException("You have already reported this content");
        }
        
        Report report = Report.builder()
            .reporter(reporter)
            .targetId(momentId)
            .targetType(ReportTargetType.MOMENT)
            .reason(request.getReason())
            .status(ReportStatus.PENDING)
            .build();
        
        reportRepository.save(report);
        log.info("User {} reported moment {}", username, momentId);
    }
    
    @Transactional
    public void reportComment(String username, Long commentId, CreateReportRequest request) {
        User reporter = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        // Check if already reported
        boolean exists = reportRepository.existsByReporterIdAndTargetIdAndTargetType(
            reporter.getId(), commentId, ReportTargetType.COMMENT);
        
        if (exists) {
            throw new AppException("You have already reported this content");
        }
        
        Report report = Report.builder()
            .reporter(reporter)
            .targetId(commentId)
            .targetType(ReportTargetType.COMMENT)
            .reason(request.getReason())
            .status(ReportStatus.PENDING)
            .build();
        
        reportRepository.save(report);
        log.info("User {} reported comment {}", username, commentId);
    }
    
    @Transactional
    public void reportUser(String username, Long userId, CreateReportRequest request) {
        User reporter = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        User reportedUser = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        // Cannot report yourself
        if (reporter.getId().equals(userId)) {
            throw new AppException("You cannot report yourself");
        }
        
        // Check if already reported
        boolean exists = reportRepository.existsByReporterIdAndTargetIdAndTargetType(
            reporter.getId(), userId, ReportTargetType.USER);
        
        if (exists) {
            throw new AppException("You have already reported this user");
        }
        
        Report report = Report.builder()
            .reporter(reporter)
            .targetId(userId)
            .targetType(ReportTargetType.USER)
            .reason(request.getReason())
            .status(ReportStatus.PENDING)
            .build();
        
        reportRepository.save(report);
        log.info("User {} reported user {}", username, userId);
    }
    
    public List<ReportResponse> getMyReports(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        List<Report> reports = reportRepository.findByReporterIdOrderByCreatedAtDesc(user.getId());
        
        return reports.stream()
            .map(this::convertToReportResponse)
            .collect(Collectors.toList());
    }
    
    private ReportResponse convertToReportResponse(Report report) {
        return ReportResponse.builder()
            .id(report.getId().toString())
            .reportedContentId(report.getTargetId().toString())
            .contentType(report.getTargetType().name())
            .reason(report.getReason())
            .status(report.getStatus().name())
            .createdAt(report.getCreatedAt())
            .build();
    }
}
