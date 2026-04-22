-- Add group avatar URL column to conversations table
ALTER TABLE conversations ADD COLUMN group_avatar_url VARCHAR(500);

-- Add index for faster queries
CREATE INDEX idx_conversations_group_avatar ON conversations(group_avatar_url);
