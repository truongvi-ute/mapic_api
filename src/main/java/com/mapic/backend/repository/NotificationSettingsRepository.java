package com.mapic.backend.repository;

import com.mapic.backend.entity.NotificationSettings;
import com.mapic.backend.entity.NotificationType;
import com.mapic.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationSettingsRepository extends JpaRepository<NotificationSettings, Long> {
    
    List<NotificationSettings> findByUser(User user);
    
    Optional<NotificationSettings> findByUserAndNotificationType(User user, NotificationType notificationType);
    
    boolean existsByUserAndNotificationType(User user, NotificationType notificationType);
}
