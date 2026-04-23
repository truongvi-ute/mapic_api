package com.mapic.backend.entity;

public enum AdminRole {
    ADMIN,          // Regular admin - can manage users and content
    MODERATOR,      // Content moderator - can moderate reports only
    SUPER_ADMIN     // Super admin - full system access
}
