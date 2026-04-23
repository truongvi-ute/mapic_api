package com.mapic.backend.service;

import com.mapic.backend.dto.request.TriggerSOSRequest;
import com.mapic.backend.dto.response.SOSAlertDTO;
import com.mapic.backend.dto.response.TriggerSOSResponse;
import com.mapic.backend.entity.*;
import com.mapic.backend.exception.ConflictException;
import com.mapic.backend.exception.ForbiddenException;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SOSServiceImpl implements SOSService {

    private final SOSAlertRepository sosAlertRepository;
    private final SOSAlertRecipientRepository sosAlertRecipientRepository;
    private final SOSAuditLogRepository sosAuditLogRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final UserStatusRepository userStatusRepository;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public TriggerSOSResponse createSOSAlert(Long userId, TriggerSOSRequest request) {
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Check if user already has an active alert
        if (sosAlertRepository.findBySenderIdAndStatus(userId, SOSAlertStatus.ACTIVE).isPresent()) {
            throw new ConflictException("You already have an active SOS alert");
        }

        // 1. Create the alert
        SOSAlert alert = SOSAlert.builder()
                .sender(sender)
                .status(SOSAlertStatus.ACTIVE)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .message(request.getMessage())
                .locationStatus(request.getLocationStatus())
                .build();

        alert = sosAlertRepository.save(alert);

        // 2. Identify recipients
        // Try to get designated SOS contacts first
        List<Friendship> friendships = friendshipRepository.findSosContactsByUserId(userId);
        
        // If no specific SOS contacts are set, fallback to all friends
        if (friendships.isEmpty()) {
            log.info("No designated SOS contacts found for user {}, falling back to all friends", userId);
            friendships = friendshipRepository.findAllFriendsByUserId(userId);
        }

        List<User> friends = friendships.stream()
                .map(f -> f.getUser1().getId().equals(userId) ? f.getUser2() : f.getUser1())
                .collect(Collectors.toList());

        List<SOSAlertRecipient> recipients = new ArrayList<>();
        List<TriggerSOSResponse.RecipientInfo> recipientInfos = new ArrayList<>();

        for (User friend : friends) {
            SOSAlertRecipient recipient = SOSAlertRecipient.builder()
                    .alert(alert)
                    .recipient(friend)
                    .hasResponded(false)
                    .build();
            recipients.add(recipient);

            recipientInfos.add(TriggerSOSResponse.RecipientInfo.builder()
                    .userId(friend.getId())
                    .name(friend.getName())
                    .avatarUrl(friend.getUserProfile() != null ? friend.getUserProfile().getAvatarUrl() : null)
                    .build());

            // 3. Create persistent notification for each friend
            notificationService.createNotification(sender, friend, NotificationType.SOS_ALERT, "SOS_ALERT", alert.getId());
        }

        sosAlertRecipientRepository.saveAll(recipients);

        // 4. Update user status to indicate they are sharing emergency location
        UserStatus status = userStatusRepository.findById(userId).orElse(null);
        if (status != null) {
            status.setIsSharingLocation(true);
            status.setLastLat(request.getLatitude());
            status.setLastLng(request.getLongitude());
            status.setLastSeenAt(LocalDateTime.now());
            userStatusRepository.save(status);
        }

        // 5. Audit log
        logSOSAction(userId, "TRIGGER", alert.getId(), "SOS Triggered at " + request.getLatitude() + ", " + request.getLongitude());

        return TriggerSOSResponse.builder()
                .alertId(alert.getId())
                .triggeredAt(alert.getTriggeredAt())
                .recipientCount(friends.size())
                .recipients(recipientInfos)
                .build();
    }

    @Override
    @Transactional
    public void resolveSOSAlert(Long alertId, Long userId) {
        SOSAlert alert = sosAlertRepository.findById(alertId)
                .orElseThrow(() -> new NotFoundException("SOS Alert not found"));

        if (!alert.getSender().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to resolve this alert");
        }

        if (alert.getStatus() == SOSAlertStatus.RESOLVED) {
            return;
        }

        alert.setStatus(SOSAlertStatus.RESOLVED);
        alert.setResolvedAt(LocalDateTime.now());
        sosAlertRepository.save(alert);

        // Audit log
        logSOSAction(userId, "RESOLVE", alertId, "SOS Resolved");
        
        log.info("SOS Alert {} resolved by user {}", alertId, userId);
    }

    @Override
    public ActiveAlertsResponse getActiveAlerts(Long userId) {
        SOSAlertDTO asSender = sosAlertRepository.findBySenderIdAndStatus(userId, SOSAlertStatus.ACTIVE)
                .map(this::convertToDTO)
                .orElse(null);

        List<SOSAlertDTO> asRecipient = sosAlertRepository.findActiveAlertsByRecipientId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new ActiveAlertsResponse(asSender, asRecipient);
    }

    @Override
    public AlertHistoryResponse getAlertHistory(Long userId, Integer limit, Integer offset) {
        List<SOSAlert> allAlerts = sosAlertRepository.findBySenderIdOrderByTriggeredAtDesc(userId);
        
        int total = allAlerts.size();
        List<SOSAlertDTO> paged = allAlerts.stream()
                .skip(offset != null ? offset : 0)
                .limit(limit != null ? limit : 20)
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new AlertHistoryResponse(paged, total, limit, offset);
    }

    @Override
    public boolean validateAlertOwnership(Long alertId, Long userId) {
        return sosAlertRepository.findById(alertId)
                .map(alert -> alert.getSender().getId().equals(userId))
                .orElse(false);
    }

    @Override
    @Transactional
    public void markAlertAsViewed(Long alertId, Long userId) {
        sosAlertRecipientRepository.findByAlertIdAndRecipientId(alertId, userId)
                .ifPresent(recipient -> {
                    if (recipient.getViewedAt() == null) {
                        recipient.setViewedAt(LocalDateTime.now());
                        sosAlertRecipientRepository.save(recipient);
                        logSOSAction(userId, "VIEW", alertId, "SOS Viewed by recipient");
                    }
                });
    }

    private SOSAlertDTO convertToDTO(SOSAlert alert) {
        int recipientCount = sosAlertRecipientRepository.findByAlertId(alert.getId()).size();
        
        return SOSAlertDTO.builder()
                .id(alert.getId())
                .senderId(alert.getSender().getId())
                .senderName(alert.getSender().getName())
                .senderAvatar(alert.getSender().getUserProfile() != null ? alert.getSender().getUserProfile().getAvatarUrl() : null)
                .triggeredAt(alert.getTriggeredAt())
                .resolvedAt(alert.getResolvedAt())
                .status(alert.getStatus())
                .latitude(alert.getLatitude())
                .longitude(alert.getLongitude())
                .message(alert.getMessage())
                .locationStatus(alert.getLocationStatus())
                .recipientCount(recipientCount)
                .build();
    }

    private void logSOSAction(Long userId, String action, Long alertId, String metadata) {
        SOSAuditLog logEntry = SOSAuditLog.builder()
                .userId(userId)
                .action(action)
                .alertId(alertId)
                .metadata(metadata)
                .build();
        sosAuditLogRepository.save(logEntry);
    }
}
