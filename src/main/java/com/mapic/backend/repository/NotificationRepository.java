package com.mapic.backend.repository;

import com.mapic.backend.entity.Notification;
import com.mapic.backend.entity.NotificationType;
import com.mapic.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Sort by priority DESC (HIGH first), then createdAt DESC (newest first)
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient " +
           "ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findByRecipientOrderByPriorityAndCreatedAt(@Param("recipient") User recipient);
    
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient " +
           "ORDER BY n.priority DESC, n.createdAt DESC")
    Page<Notification> findByRecipientOrderByPriorityAndCreatedAt(@Param("recipient") User recipient, Pageable pageable);
    
    long countByRecipientAndIsReadFalse(User recipient);
    
    // For aggregation: find recent notification of same type and target (within 24 hours)
    @Query("SELECT n FROM Notification n WHERE n.recipient = :recipient " +
           "AND n.type = :type AND n.targetType = :targetType AND n.targetId = :targetId " +
           "AND n.createdAt > :since ORDER BY n.createdAt DESC")
    Optional<Notification> findRecentSimilarNotification(
        @Param("recipient") User recipient,
        @Param("type") NotificationType type,
        @Param("targetType") String targetType,
        @Param("targetId") Long targetId,
        @Param("since") LocalDateTime since
    );
    
    // For cleanup job: find old notifications
    @Query("SELECT n FROM Notification n WHERE n.createdAt < :before")
    List<Notification> findOldNotifications(@Param("before") LocalDateTime before);
}

