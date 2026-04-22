package com.mapic.backend.service;

import com.mapic.backend.entity.NotificationSettings;
import com.mapic.backend.entity.NotificationType;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.NotificationSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSettingsService {

    private final NotificationSettingsRepository notificationSettingsRepository;

    @Transactional(readOnly = true)
    public List<NotificationSettings> getUserSettings(User user) {
        List<NotificationSettings> settings = notificationSettingsRepository.findByUser(user);
        
        // If user has no settings, create default settings for all types
        if (settings.isEmpty()) {
            settings = createDefaultSettings(user);
        }
        
        return settings;
    }

    @Transactional
    public NotificationSettings updateSetting(User user, NotificationType type, 
                                             Boolean enabled, Boolean pushEnabled, Boolean soundEnabled) {
        NotificationSettings settings = notificationSettingsRepository
                .findByUserAndNotificationType(user, type)
                .orElse(NotificationSettings.builder()
                        .user(user)
                        .notificationType(type)
                        .build());
        
        if (enabled != null) settings.setEnabled(enabled);
        if (pushEnabled != null) settings.setPushEnabled(pushEnabled);
        if (soundEnabled != null) settings.setSoundEnabled(soundEnabled);
        
        return notificationSettingsRepository.save(settings);
    }

    @Transactional
    public List<NotificationSettings> createDefaultSettings(User user) {
        List<NotificationSettings> settings = new ArrayList<>();
        
        for (NotificationType type : NotificationType.values()) {
            if (!notificationSettingsRepository.existsByUserAndNotificationType(user, type)) {
                NotificationSettings setting = NotificationSettings.builder()
                        .user(user)
                        .notificationType(type)
                        .enabled(true)
                        .pushEnabled(true)
                        .soundEnabled(true)
                        .build();
                settings.add(notificationSettingsRepository.save(setting));
            }
        }
        
        log.info("Created default notification settings for user {}", user.getUsername());
        return settings;
    }
}
