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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminContentService {

    private final ReportRepository reportRepository;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final IStorageService storageService;

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
        log.info("[MODERATION] Starting moderation for report {} with action {}", reportId, request.getAction());
        log.info("[MODERATION] Request details - reason: {}, note: {}", request.getReason(), request.getNote());
        
        Long id = Long.parseLong(reportId);
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Report not found with ID: " + reportId));
        
        log.info("[MODERATION] Found report - targetType: {}, targetId: {}, status: {}", 
                 report.getTargetType(), report.getTargetId(), report.getStatus());
        
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
        
        // Update report status based on action
        if ("IGNORE".equals(request.getAction())) {
            report.setStatus(ReportStatus.DISMISSED);
        } else {
            report.setStatus(ReportStatus.RESOLVED);
        }
        reportRepository.save(report);
        
        log.info("[MODERATION] Report {} moderated successfully - new status: {}", reportId, report.getStatus());
    }

    private void moderateMoment(Report report, ModerationActionRequest request) {
        log.info("[MODERATION-MOMENT] Processing moment {} with action {}", report.getTargetId(), request.getAction());
        
        Moment moment = momentRepository.findById(report.getTargetId())
            .orElseThrow(() -> new NotFoundException("Moment not found with ID: " + report.getTargetId()));
        
        log.info("[MODERATION-MOMENT] Current moment status: {}", moment.getStatus());
        
        switch (request.getAction()) {
            case "DELETE":
                moment.setStatus("DELETED");
                momentRepository.save(moment);
                log.info("[MODERATION-MOMENT] Moment {} marked as DELETED", moment.getId());
                break;
            case "HIDE":
                moment.setStatus("HIDDEN");
                momentRepository.save(moment);
                log.info("[MODERATION-MOMENT] Moment {} marked as HIDDEN", moment.getId());
                break;
            case "APPROVE":
                // Content is OK, ensure it's not hidden/deleted
                if ("HIDDEN".equals(moment.getStatus()) || "DELETED".equals(moment.getStatus())) {
                    moment.setStatus("ACTIVE");
                    momentRepository.save(moment);
                }
                log.info("[MODERATION-MOMENT] Moment {} approved", moment.getId());
                break;
            case "IGNORE":
                // Ignore the report, don't change moment status
                log.info("[MODERATION-MOMENT] Report for moment {} ignored", moment.getId());
                break;
            case "WARN":
                // Warn user but don't change content status
                log.info("[MODERATION-MOMENT] Warning issued for moment {}, no status change", moment.getId());
                break;
            default:
                log.warn("[MODERATION-MOMENT] Unknown action: {}", request.getAction());
                break;
        }
    }

    private void moderateComment(Report report, ModerationActionRequest request) {
        log.info("[MODERATION-COMMENT] Processing comment {} with action {}", report.getTargetId(), request.getAction());
        
        Comment comment = commentRepository.findById(report.getTargetId())
            .orElseThrow(() -> new NotFoundException("Comment not found with ID: " + report.getTargetId()));
        
        // Comment không có status field, chỉ có thể delete
        switch (request.getAction()) {
            case "DELETE":
                commentRepository.delete(comment);
                log.info("[MODERATION-COMMENT] Comment {} deleted", comment.getId());
                break;
            case "APPROVE":
            case "IGNORE":
            case "WARN":
                // For comments, these actions just resolve the report without deleting
                log.info("[MODERATION-COMMENT] Comment {} - action {} applied (no deletion)", 
                         comment.getId(), request.getAction());
                break;
            case "HIDE":
                log.warn("[MODERATION-COMMENT] HIDE not supported for comments (no status field). Use DELETE instead.");
                break;
            default:
                log.warn("[MODERATION-COMMENT] Unknown action: {}", request.getAction());
                break;
        }
    }

    private void moderateUser(Report report, ModerationActionRequest request) {
        log.info("[MODERATION-USER] Processing user {} with action {}", report.getTargetId(), request.getAction());
        
        User user = userRepository.findById(report.getTargetId())
            .orElseThrow(() -> new NotFoundException("User not found with ID: " + report.getTargetId()));
        
        log.info("[MODERATION-USER] Current user status: {}", user.getStatus());
        
        switch (request.getAction()) {
            case "DELETE":
            case "HIDE":
                // Block user
                user.setStatus(AccountStatus.BLOCK);
                userRepository.save(user);
                log.info("[MODERATION-USER] User {} blocked", user.getId());
                break;
            case "WARN":
                // Just warn, don't block
                log.info("[MODERATION-USER] Warning issued to user {}", user.getId());
                // TODO: Implement warning system (notification, warning count, etc.)
                break;
            case "APPROVE":
            case "IGNORE":
                // Report is invalid, don't change user status
                log.info("[MODERATION-USER] User {} - action {} applied (no status change)", 
                         user.getId(), request.getAction());
                break;
            default:
                log.warn("[MODERATION-USER] Unknown action: {}", request.getAction());
                break;
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
        String firstMediaUrl = null;
        List<String> allMediaUrls = new ArrayList<>();
        int contentReportCount = 0;
        int totalReports = 0;
        
        try {
            switch (report.getTargetType()) {
                case MOMENT:
                    Moment moment = momentRepository.findById(report.getTargetId()).orElse(null);
                    if (moment != null) {
                        reportedUser = moment.getAuthor();
                        contentPreview = moment.getContent();
                        
                        // Get media URLs and resolve them to full paths
                        if (moment.getMedia() != null && !moment.getMedia().isEmpty()) {
                            allMediaUrls = moment.getMedia().stream()
                                .sorted(Comparator.comparing(MomentMedia::getSortOrder))
                                .map(media -> storageService.resolveUrl(media.getMediaUrl(), "moments"))
                                .collect(Collectors.toList());
                            firstMediaUrl = allMediaUrls.isEmpty() ? null : allMediaUrls.get(0);
                        }
                        
                        // Count reports for THIS moment
                        contentReportCount = reportRepository.countByTargetIdAndTargetType(
                            moment.getId(), ReportTargetType.MOMENT);
                        
                        // Total reports for the user
                        totalReports = reportRepository.countByTargetIdAndTargetType(
                            reportedUser.getId(), ReportTargetType.USER);
                    }
                    break;
                case COMMENT:
                    Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
                    if (comment != null) {
                        reportedUser = comment.getAuthor();
                        contentPreview = comment.getContent();
                        
                        // Count reports for THIS comment
                        contentReportCount = reportRepository.countByTargetIdAndTargetType(
                            comment.getId(), ReportTargetType.COMMENT);
                        
                        totalReports = reportRepository.countByTargetIdAndTargetType(
                            reportedUser.getId(), ReportTargetType.USER);
                    }
                    break;
                case USER:
                    reportedUser = userRepository.findById(report.getTargetId()).orElse(null);
                    if (reportedUser != null) {
                        contentReportCount = reportRepository.countByTargetIdAndTargetType(
                            reportedUser.getId(), ReportTargetType.USER);
                        totalReports = contentReportCount;
                    }
                    break;
            }
        } catch (Exception e) {
            log.warn("Error fetching reported content details: {}", e.getMessage());
        }
        
        // Determine reason category
        String reasonCategory = report.getReasonCategory() != null 
            ? report.getReasonCategory().name() 
            : categorizeReason(report.getReason());
        
        return ReportResponse.builder()
            .id(report.getId().toString())
            .reportedContentId(report.getTargetId().toString())
            .contentType(report.getTargetType().name())
            .reasonCategory(reasonCategory)
            .reason(report.getReason())
            .description(report.getReason())
            .status(report.getStatus().name())
            .createdAt(report.getCreatedAt())
            .reporter(ReportResponse.ReporterInfo.builder()
                .id(reporter.getId().toString())
                .username(reporter.getUsername())
                .name(reporter.getName())
                .avatarUrl(reporter.getUserProfile() != null 
                    ? storageService.resolveUrl(reporter.getUserProfile().getAvatarUrl(), "avatars")
                    : null)
                .build())
            .reportedUser(reportedUser != null ? ReportResponse.ReportedUserInfo.builder()
                .id(reportedUser.getId().toString())
                .username(reportedUser.getUsername())
                .name(reportedUser.getName())
                .avatarUrl(reportedUser.getUserProfile() != null 
                    ? storageService.resolveUrl(reportedUser.getUserProfile().getAvatarUrl(), "avatars")
                    : null)
                .totalReports(totalReports)
                .isBanned(reportedUser.getStatus() == AccountStatus.BLOCK)
                .build() : null)
            .content(ReportResponse.ContentInfo.builder()
                .id(report.getTargetId().toString())
                .type(report.getTargetType().name())
                .content(contentPreview)
                .mediaUrl(firstMediaUrl)
                .mediaUrls(allMediaUrls)
                .createdAt(report.getCreatedAt())
                .isDeleted(false)
                .reportCount(contentReportCount)
                .build())
            .build();
    }
    
    // Helper method to categorize old free-text reasons
    private String categorizeReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            log.warn("[CATEGORIZE] Reason is null or empty, defaulting to OTHER");
            return ReportReasonCategory.OTHER.name();
        }
        
        String r = reason.toLowerCase().trim();
        log.debug("[CATEGORIZE] Categorizing reason: {}", r);
        
        // Spam patterns
        if (r.contains("spam") || r.contains("quảng cáo") || r.contains("quang cao") || 
            r.contains("rác") || r.contains("rac") || r.contains("advertisement")) {
            log.debug("[CATEGORIZE] Matched SPAM");
            return ReportReasonCategory.SPAM.name();
        }
        
        // Harassment patterns
        if (r.contains("quấy rối") || r.contains("quay roi") || r.contains("bắt nạt") || 
            r.contains("bat nat") || r.contains("harassment") || r.contains("bully") ||
            r.contains("đe dọa") || r.contains("de doa") || r.contains("threat")) {
            log.debug("[CATEGORIZE] Matched HARASSMENT");
            return ReportReasonCategory.HARASSMENT.name();
        }
        
        // Violence patterns
        if (r.contains("bạo lực") || r.contains("bao luc") || r.contains("violence") || 
            r.contains("đánh nhau") || r.contains("danh nhau") || r.contains("máu me") ||
            r.contains("mau me") || r.contains("gore")) {
            log.debug("[CATEGORIZE] Matched VIOLENCE");
            return ReportReasonCategory.VIOLENCE.name();
        }
        
        // Fake news patterns
        if (r.contains("sai lệch") || r.contains("sai lech") || r.contains("giả mạo") || 
            r.contains("gia mao") || r.contains("fake") || r.contains("tin giả") ||
            r.contains("tin gia") || r.contains("misinformation") || r.contains("hoax")) {
            log.debug("[CATEGORIZE] Matched FAKE_NEWS");
            return ReportReasonCategory.FAKE_NEWS.name();
        }
        
        // Hate speech patterns
        if (r.contains("thù ghét") || r.contains("thu ghet") || r.contains("phân biệt") || 
            r.contains("phan biet") || r.contains("hate") || r.contains("racist") ||
            r.contains("discrimination") || r.contains("kỳ thị") || r.contains("ky thi")) {
            log.debug("[CATEGORIZE] Matched HATE_SPEECH");
            return ReportReasonCategory.HATE_SPEECH.name();
        }
        
        // Inappropriate content patterns
        if (r.contains("không phù hợp") || r.contains("khong phu hop") || 
            r.contains("inappropriate") || r.contains("nude") || r.contains("khỏa thân") ||
            r.contains("khoa than") || r.contains("sex") || r.contains("porn") ||
            r.contains("18+") || r.contains("nhạy cảm") || r.contains("nhay cam")) {
            log.debug("[CATEGORIZE] Matched INAPPROPRIATE");
            return ReportReasonCategory.INAPPROPRIATE.name();
        }
        
        log.debug("[CATEGORIZE] No match found, defaulting to OTHER");
        return ReportReasonCategory.OTHER.name();
    }
}
