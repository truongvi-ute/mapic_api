package com.mapic.backend.dto;

import com.mapic.backend.entity.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReactionDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long momentId;
    private ReactionType type;
    private LocalDateTime createdAt;
}
