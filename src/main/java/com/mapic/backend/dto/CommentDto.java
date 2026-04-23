package com.mapic.backend.dto;

import com.mapic.backend.entity.ReactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private AuthorDto author;
    private Long momentId;
    private Long parentCommentId;
    private LocalDateTime createdAt;
    private Long reactionCount;
    private Boolean userReacted;
    private ReactionType userReactionType; // Thêm field này để biết user đã react loại gì
    private List<CommentDto> replies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private Long id;
        private String username;
        private String fullName;
        private String avatarUrl;
    }
}
