package com.mapic.backend.dto;

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
