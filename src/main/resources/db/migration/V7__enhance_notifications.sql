-- Add new fields to notifications table
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS priority VARCHAR(20) DEFAULT 'NORMAL';
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS thumbnail_url VARCHAR(500);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS content_preview VARCHAR(200);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS actor_ids VARCHAR(1000);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS actor_count INTEGER DEFAULT 1;

-- Add expo push token to user_profiles
ALTER TABLE user_profiles ADD COLUMN IF NOT EXISTS expo_push_token VARCHAR(200);

-- Create notification_settings table
CREATE TABLE IF NOT EXISTS notification_settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    notification_type VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sound_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE(user_id, notification_type)
);

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_notifications_priority_created ON notifications(priority DESC, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient_type_target ON notifications(recipient_id, type, target_type, target_id, created_at);
CREATE INDEX IF NOT EXISTS idx_notification_settings_user_type ON notification_settings(user_id, notification_type);
