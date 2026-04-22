package com.mapic.backend.entity;

public enum NotificationPriority {
    HIGH,    // SOS_ALERT - urgent, needs immediate attention
    NORMAL,  // FRIEND_REQUEST, FRIEND_ACCEPT, NEW_MESSAGE
    LOW      // MOMENT_REACTION, MOMENT_COMMENT, MOMENT_TAG
}
