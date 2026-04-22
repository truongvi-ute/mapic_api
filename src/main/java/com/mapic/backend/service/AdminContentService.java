package com.mapic.backend.service;

import com.mapic.backend.dto.request.ModerationActionRequest;
import com.mapic.backend.dto.response.ContentAnalyticsResponse;
import com.mapic.backend.dto.response.ReportResponse;
import com.mapic.backend.entity.*;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.repository.CommentRepository;
import com.mapic.backend.repository.MomentRepository;
import com.mapic.backend.repository.ReportRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminContentService {

    private final ReportRepository reportRepository;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public Page<ReportResponse> getReportQueue(int page, int size, String type, String status, String search) {
        log.info("Getting report queue - page: {}, size: {}, type: {}, status: {}", page, size, type, status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Report> reportsPage;
        
        ReportStatus reportStatus = status != null ? ReportStatus.valueOf(status) : null;
        ReportTargetType targetType = type != null ? ReportTargetType.valueOf(type) : null;
        
        reportsPage = reportRepository.findWithFilters(reportStatus, targetType, pageable);
        
        return reportsPage.map(this::convertToReportResponse);
    }

    @Transactional
    public void moderateContent(String reportId, ModerationActionRequest request) {
        log.info("Moderating report {} with action {}", reportId, request.getAction());
        
        Long id = Long.parseLong(reportId);
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Report not found"));
        
        // Handle moderation based on target type
        switch (report.getTargetType()) {
            case MOMENT:
                moderateMoment(report, request);
                break;
            case COMMENT:
                moderateComment(report, request);
                break;
            case USER:
                moderateUser(report, request);
                break;
        }
        
        // Update report status
        report.setStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);
        
        log.info("Report {} moderated successfully", reportId);
    }

    private void moderateMoment(Report report, ModerationActionRequest request) {
        Moment moment = momentRepository.findById(report.getTargetId())
            .orElseThrow(() -> new NotFoundException("Moment not found"));
        
        switch (request.getAction()) {
            case "DELETE":
                moment.setStatus("DELETED");
                momentRepository.save(moment);
                log.info("Moment {} deleted", moment.getId());
                break;
            case "HIDE":
                moment.setStatus("HIDDEN");
                momentRepository.save(moment);
                log.info("Moment {} hidden", moment.getId());
                break;
            case "APPROVE":
                // Content is OK, do nothing
                log.info("Moment {} approved", moment.getId());
                break;
            case "IGNORE":
                // Ignore the report
                log.info("Report for moment {} ignored", moment.getId());
                break;
        }
    }

    private void moderateComment(Report report, ModerationActionRequest request) {
        Comment comment = commentRepository.findById(report.getTargetId())
            .orElseThrow(() -> new NotFoundException("Comment not found"));
        
        // Comment không có status field, chỉ có thể delete
        if ("DELETE".equals(request.getAction())) {
            commentRepository.delete(comment);
            log.info("Comment {} deleted", comment.getId());
        } else {
            log.warn("Comment moderation only supports DELETE action. Action {} ignored.", request.getAction());
        }
    }

    private void moderateUser(Report report, ModerationActionRequest request) {
        User user = userRepository.findById(report.getTargetId())
            .orElseThrow(() -> new NotFoundException("User not found"));
        
        if ("DELETE".equals(request.getAction()) || "HIDE".equals(request.getAction())) {
            // Block user
            user.setStatus(AccountStatus.BLOCK);
            userRepository.save(user);
            log.info("User {} blocked", user.getId());
        }
    }

    public ContentAnalyticsResponse getContentAnalytics(String period, String type) {
        log.info("Getting content analytics - period: {}, type: {}", period, type);
        
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        long resolvedReports = reportRepository.countByStatus(ReportStatus.RESOLVED);
        
        long totalMoments = momentRepository.count();
        long totalComments = commentRepository.count();
        
        double resolutionRate = totalReports > 0 ? (double) resolvedReports / totalReports * 100 : 0;
        
        return ContentAnalyticsResponse.builder()
            .overall(ContentAnalyticsResponse.OverallStats.builder()
                .totalReports((int) totalReports)
                .pendingReports((int) pendingReports)
                .resolvedReports((int) resolvedReports)
                .totalMoments((int) totalMoments)
                .totalComments((int) totalComments)
                .resolutionRate(resolutionRate)
                .build())
            .build();
    }

    public ReportResponse getReportDetails(String reportId) {
        log.info("Getting report details for ID: {}", reportId);
        
        Long id = Long.parseLong(reportId);
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Report not found"));
        
        return convertToReportResponse(report);
    }

    @Transactional
    public void approveContent(String reportId) {
        log.info("Approving content for report {}", reportId);
        
        Long id = Long.parseLong(reportId);
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Report not found"));
        
        // Mark report as resolved (content is OK)
        report.setStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);
    }

    @Transactional
    public void rejectContent(String reportId, String reason) {
        log.info("Rejecting content for report {} with reason: {}", reportId, reason);
        
        Long id = Long.parseLong(reportId);
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Report not found"));
        
        // Delete/hide the content
        ModerationActionRequest request = new ModerationActionRequest();
        request.setAction("DELETE");
        request.setReason(reason);
        
        moderateContent(reportId, request);
    }

    public Object getContentStatistics() {
        log.info("Getting content statistics");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMoments", momentRepository.count());
        stats.put("totalComments", commentRepository.count());
        stats.put("totalReports", reportRepository.count());
        stats.put("pendingReports", reportRepository.countByStatus(ReportStatus.PENDING));
        stats.put("resolvedReports", reportRepository.countByStatus(ReportStatus.RESOLVED));
        
        // Reports by type
        List<Object[]> reportsByType = reportRepository.countByTargetType();
        Map<String, Long> typeStats = reportsByType.stream()
            .collect(Collectors.toMap(
                arr -> ((ReportTargetType) arr[0]).name(),
                arr -> (Long) arr[1]
            ));
        stats.put("reportsByType", typeStats);
        
        return stats;
    }
    
    private ReportResponse convertToReportResponse(Report report) {
        User reporter = report.getReporter();
        
        // Get reported user based on target type
        User reportedUser = null;
        String contentPreview = null;
        int totalReports = 0;
        
        try {
            switch (report.getTargetType()) {
                case MOMENT:
                    Moment moment = momentRepository.findById(report.getTargetId()).orElse(null);
                    if (moment != null) {
                        reportedUser = moment.getAuthor();
                        contentPreview = moment.getContent();
                        totalReports = reportRepository.countByTargetIdAndTargetType(
                            reportedUser.getId(), ReportTargetType.USER);
                    }
                    break;
                case COMMENT:
                    Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
                    if (comment != null) {
                        reportedUser = comment.getAuthor();
                        contentPreview = comment.getContent();
                        totalReports = reportRepository.countByTargetIdAndTargetType(
                            reportedUser.getId(), ReportTargetType.USER);
                    }
                    break;
                case USER:
                    reportedUser = userRepository.findById(report.getTargetId()).orElse(null);
                    if (reportedUser != null) {
                        totalReports = reportRepository.countByTargetIdAndTargetType(
                            reportedUser.getId(), ReportTargetType.USER);
                    }
                    break;
            }
        } catch (Exception e) {
            log.warn("Error fetching reported content details: {}", e.getMessage());
        }
        
        return ReportResponse.builder()
            .id(report.getId().toString())
            .reportedContentId(report.getTargetId().toString())
            .contentType(report.getTargetType().name())
            .reason(report.getReason())
            .status(report.getStatus().name())
            .createdAt(report.getCreatedAt())
            .reporter(ReportResponse.ReporterInfo.builder()
                .id(reporter.getId().toString())
                .username(reporter.getUsername())
                .name(reporter.getName())
                .avatarUrl(reporter.getUserProfile() != null ? reporter.getUserProfile().getAvatarUrl() : null)
                .build())
            .reportedUser(reportedUser != null ? ReportResponse.ReportedUserInfo.builder()
                .id(reportedUser.getId().toString())
                .username(reportedUser.getUsername())
                .name(reportedUser.getName())
                .avatarUrl(reportedUser.getUserProfile() != null ? reportedUser.getUserProfile().getAvatarUrl() : null)
                .totalReports(totalReports)
                .isBanned(reportedUser.getStatus() == AccountStatus.BLOCK)
                .build() : null)
            .content(ReportResponse.ContentInfo.builder()
                .id(report.getTargetId().toString())
                .type(report.getTargetType().name())
                .content(contentPreview)
                .createdAt(report.getCreatedAt())
                .isDeleted(false)
                .build())
            .build();
    }
}
