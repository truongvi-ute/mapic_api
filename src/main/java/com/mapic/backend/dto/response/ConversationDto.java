package com.mapic.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private Long id;
    @JsonProperty("isGroup")
    private boolean isGroup;
    private String title;        // null for 1-1 chats
    private Long creatorId;
    private LocalDateTime createdAt;
    private MessageDto lastMessage;
    private List<ParticipantDto> participants;
    private int unreadCount;
}
