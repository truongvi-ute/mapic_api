package com.mapic.backend.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderUsername;
    private String senderName; // Full name of sender
    private String senderAvatarUrl;
    private String type; // TEXT, SHARE_MOMENT, SHARE_ALBUM
    private String content;
    private Long referenceId;       // momentId or albumId if share
    private Object sharedPreview;   // momentDto or albumDto snippet
    private Map<String, Long> reactions; // emoji -> count
    private String myReaction;      // the current user's reaction
    private LocalDateTime createdAt;
}
