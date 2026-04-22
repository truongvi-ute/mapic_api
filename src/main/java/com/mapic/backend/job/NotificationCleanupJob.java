package com.mapic.backend.job;

import com.mapic.backend.entity.Notification;
import com.mapic.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationCleanupJob {

    private final NotificationRepository notificationRepository;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldNotifications() {
        log.info("Starting notification cleanup job...");
        
        // Delete notifications older than 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Notification> oldNotifications = notificationRepository.findOldNotifications(thirtyDaysAgo);
        
        if (!oldNotifications.isEmpty()) {
            notificationRepository.deleteAll(oldNotifications);
            log.info("Deleted {} old notifications (older than 30 days)", oldNotifications.size());
        } else {
            log.info("No old notifications to delete");
        }
    }
}
