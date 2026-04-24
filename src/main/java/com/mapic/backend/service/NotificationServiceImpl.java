package com.mapic.backend.service;

import com.mapic.backend.dto.NotificationDTO;
import com.mapic.backend.entity.*;
import com.mapic.backend.repository.NotificationRepository;
import com.mapic.backend.repository.NotificationSettingsRepository;
import com.mapic.backend.repository.MomentRepository;
import com.mapic.backend.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final MomentRepository momentRepository;
    private final CommentRepository commentRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public void createNotification(User actor, User recipient, NotificationType type, String targetType, Long targetId) {
        if (actor.getId().equals(recipient.getId())) {
            return; // Don't notify yourself
        }

        // Check if user has disabled this notification type
        Optional<NotificationSettings> settings = notificationSettingsRepository
                .findByUserAndNotificationType(recipient, type);
        if (settings.isPresent() && !settings.get().getEnabled()) {
            log.info("Notification type {} disabled for user {}", type, recipient.getUsername());
            return;
        }

        // Determine priority based on type
        NotificationPriority priority = getPriorityForType(type);

        // Get rich media (thumbnail, preview)
        String thumbnailUrl = null;
        String contentPreview = null;
        
        if ("MOMENT".equals(targetType) && targetId != null) {
            Optional<Moment> momentOpt = momentRepository.findById(targetId);
            if (momentOpt.isPresent()) {
                Moment moment = momentOpt.get();
                // Get first media as thumbnail
                if (moment.getMedia() != null && !moment.getMedia().isEmpty()) {
                    thumbnailUrl = moment.getMedia().get(0).getMediaUrl();
                }
            }
        }
        
        if (type == NotificationType.MOMENT_COMMENT && targetId != null) {
            // For comments, targetId is commentId, get comment content as preview
            Optional<Comment> commentOpt = commentRepository.findById(targetId);
            if (commentOpt.isPresent()) {
                Comment comment = commentOpt.get();
                String content = comment.getContent();
                contentPreview = content.length() > 50 ? content.substring(0, 50) + "..." : content;
                
                // Also get moment thumbnail
                if (comment.getMoment() != null && comment.getMoment().getMedia() != null 
                        && !comment.getMoment().getMedia().isEmpty()) {
                    thumbnailUrl = comment.getMoment().getMedia().get(0).getMediaUrl();
                }
            }
        }

        // Check for aggregation: find similar notification within 24 hours
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        Optional<Notification> existingNotification = notificationRepository
                .findRecentSimilarNotification(recipient, type, targetType, targetId, since);

        if (existingNotification.isPresent() && shouldAggregate(type)) {
            // Aggregate: update existing notification
            Notification notification = existingNotification.get();
            
            // Add actor to actorIds list
            String actorIds = notification.getActorIds();
            Set<String> actorIdSet = new HashSet<>();
            if (actorIds != null && !actorIds.isEmpty()) {
                actorIdSet.addAll(Arrays.asList(actorIds.split(",")));
            }
            actorIdSet.add(actor.getId().toString());
            
            notification.setActorIds(String.join(",", actorIdSet));
            notification.setActorCount(actorIdSet.size());
            notification.setActor(actor); // Update to latest actor
            notification.setCreatedAt(LocalDateTime.now()); // Update timestamp
            notification.setIsRead(false); // Mark as unread again
            
            notificationRepository.save(notification);
            log.info("Aggregated notification type {} for user {} (total actors: {})", 
                    type, recipient.getUsername(), actorIdSet.size());
            
            // Push real-time via WebSocket
            pushNotification(notification);
        } else {
            // Create new notification
            Notification notification = Notification.builder()
                    .actor(actor)
                    .recipient(recipient)
                    .type(type)
                    .targetType(targetType)
                    .targetId(targetId)
                    .isRead(false)
                    .priority(priority)
                    .thumbnailUrl(thumbnailUrl)
                    .contentPreview(contentPreview)
                    .actorIds(actor.getId().toString())
                    .actorCount(1)
                    .build();

            notification = notificationRepository.save(notification);
            log.info("Created notification type {} for user {} with priority {}", 
                    type, recipient.getUsername(), priority);

            // Push real-time via WebSocket
            pushNotification(notification);
        }
    }

    @Override
    public Page<NotificationDTO> getNotificationsForUser(User user, Pageable pageable) {
        return notificationRepository.findByRecipientOrderByPriorityAndCreatedAt(user, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public long getUnreadCount(User user) {
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new SecurityException("Not authorized to read this notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.findByRecipientOrderByPriorityAndCreatedAt(user).forEach(n -> {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
    }

    private void pushNotification(Notification notification) {
        try {
            NotificationDTO dto = convertToDTO(notification);
            log.info("[NOTIFICATION] Pushing to user: {} via WebSocket, type: {}", 
                    notification.getRecipient().getUsername(), notification.getType());
            messagingTemplate.convertAndSendToUser(
                    notification.getRecipient().getUsername(),  // Sử dụng username thay vì userId
                    "/queue/notifications",
                    dto
            );
            log.info("[NOTIFICATION] ✅ Successfully pushed notification to user: {}", notification.getRecipient().getUsername());
        } catch (Exception e) {
            log.error("[NOTIFICATION] ❌ Failed to push notification via WebSocket to user: {}", 
                    notification.getRecipient().getUsername(), e);
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        String actorName = notification.getActor() != null ? notification.getActor().getName() : "Hệ thống";
        String avatar = (notification.getActor() != null && notification.getActor().getUserProfile() != null)
                ? notification.getActor().getUserProfile().getAvatarUrl() : null;

        // Parse actorIds for aggregation
        List<Long> actorIds = new ArrayList<>();
        List<String> actorAvatars = new ArrayList<>();
        if (notification.getActorIds() != null && !notification.getActorIds().isEmpty()) {
            actorIds = Arrays.stream(notification.getActorIds().split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            // Note: For performance, we only return the main actor's avatar
            // Frontend can fetch other avatars if needed
            actorAvatars.add(avatar);
        }

        return NotificationDTO.builder()
                .id(notification.getId())
                .actorId(notification.getActor() != null ? notification.getActor().getId() : null)
                .actorName(actorName)
                .actorAvatar(avatar)
                .recipientId(notification.getRecipient().getId())
                .type(notification.getType())
                .targetType(notification.getTargetType())
                .targetId(notification.getTargetId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .message(generateMessage(notification))
                .priority(notification.getPriority())
                .thumbnailUrl(notification.getThumbnailUrl())
                .contentPreview(notification.getContentPreview())
                .actorIds(actorIds)
                .actorCount(notification.getActorCount())
                .actorAvatars(actorAvatars)
                .build();
    }

    private String generateMessage(Notification notification) {
        String actorName = notification.getActor() != null ? notification.getActor().getName() : "Ai đó";
        int actorCount = notification.getActorCount();
        
        // Handle aggregation
        String actorText = actorName;
        if (actorCount > 1) {
            int others = actorCount - 1;
            actorText = actorName + " và " + others + " người khác";
        }
        
        return switch (notification.getType()) {
            case FRIEND_REQUEST -> actorName + " đã gửi cho bạn lời mời kết bạn.";
            case FRIEND_ACCEPT -> actorName + " đã chấp nhận lời mời kết bạn của bạn.";
            case MOMENT_REACTION -> actorText + " đã thích bài viết của bạn.";
            case MOMENT_COMMENT -> actorText + " đã bình luận về bài viết của bạn.";
            case MOMENT_TAG -> actorText + " đã gắn thẻ bạn trong một bài viết.";
            case NEW_MESSAGE -> actorName + " đã gửi cho bạn một tin nhắn.";
            case SOS_ALERT -> "⚠️ CẢNH BÁO SOS: " + actorName + " đang cần giúp đỡ!";
            default -> "Bạn có thông báo mới từ " + actorName;
        };
    }

    private NotificationPriority getPriorityForType(NotificationType type) {
        return switch (type) {
            case SOS_ALERT -> NotificationPriority.HIGH;
            case FRIEND_REQUEST, FRIEND_ACCEPT, NEW_MESSAGE -> NotificationPriority.NORMAL;
            case MOMENT_REACTION, MOMENT_COMMENT, MOMENT_TAG -> NotificationPriority.LOW;
            default -> NotificationPriority.NORMAL;
        };
    }

    private boolean shouldAggregate(NotificationType type) {
        // Only aggregate reactions and comments
        return type == NotificationType.MOMENT_REACTION || type == NotificationType.MOMENT_COMMENT;
    }
}
