package com.mapic.backend.service;

import com.mapic.backend.dto.NotificationDTO;
import com.mapic.backend.entity.NotificationType;
import com.mapic.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface INotificationService {
    void createNotification(User actor, User recipient, NotificationType type, String targetType, Long targetId);
    
    Page<NotificationDTO> getNotificationsForUser(User user, Pageable pageable);
    
    long getUnreadCount(User user);
    
    void markAsRead(Long notificationId, User user);
    
    void markAllAsRead(User user);
}
