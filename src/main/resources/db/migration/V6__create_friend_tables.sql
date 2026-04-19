-- Create friend_requests table
CREATE TABLE IF NOT EXISTS friend_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_friend_request_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_request_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_friend_request UNIQUE (sender_id, receiver_id),
    
    INDEX idx_friend_request_receiver (receiver_id),
    INDEX idx_friend_request_status (status),
    INDEX idx_friend_request_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Drop old friendships table if exists
DROP TABLE IF EXISTS friendships;

-- Create new friendships table (only for accepted friendships)
CREATE TABLE IF NOT EXISTS friendships (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_friendship_user1 FOREIGN KEY (user_id_1) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_user2 FOREIGN KEY (user_id_2) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_friendship UNIQUE (user_id_1, user_id_2),
    
    INDEX idx_friendship_user1 (user_id_1),
    INDEX idx_friendship_user2 (user_id_2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
