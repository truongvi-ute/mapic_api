package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.CommentDto;
import com.mapic.backend.dto.ReactionDTO;
import com.mapic.backend.dto.request.CommentRequest;
import com.mapic.backend.controller.ReactionController.ReactionRequest;
import com.mapic.backend.service.ICommentService;
import com.mapic.backend.service.IReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final ICommentService commentService;
    private final IReactionService reactionService;

    @PostMapping("/moment/{momentId}")
    public ResponseEntity<ApiResponse<CommentDto>> createComment(
            @PathVariable Long momentId,
            @RequestBody CommentRequest request) {
        log.info("Request to create comment on moment: {}", momentId);
        CommentDto commentDto = commentService.createComment(momentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment created successfully", commentDto));
    }

    @GetMapping("/moment/{momentId}")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getCommentsByMoment(
            @PathVariable Long momentId) {
        log.info("Request to fetch comments for moment: {}", momentId);
        List<CommentDto> comments = commentService.getCommentsByMoment(momentId);
        return ResponseEntity.ok(ApiResponse.success("Fetched comments successfully", comments));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        log.info("Request to delete comment: {}", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }

    @PostMapping("/{commentId}/react")
    public ResponseEntity<ApiResponse<ReactionDTO>> toggleReaction(
            @PathVariable Long commentId,
            @RequestBody ReactionRequest request) {
        log.info("Toggle reaction {} on comment {}", request.getType(), commentId);
        ReactionDTO reaction = reactionService.toggleCommentReaction(commentId, request.getType());
        if (reaction == null) {
            return ResponseEntity.ok(ApiResponse.success("Reaction removed successfully", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Reaction added successfully", reaction));
    }

    @DeleteMapping("/{commentId}/react")
    public ResponseEntity<ApiResponse<Void>> removeReaction(@PathVariable Long commentId) {
        log.info("Remove reaction from comment {}", commentId);
        reactionService.removeCommentReaction(commentId);
        return ResponseEntity.ok(ApiResponse.success("Reaction removed successfully", null));
    }

    @GetMapping("/{commentId}/react/count")
    public ResponseEntity<ApiResponse<Long>> getReactionCount(@PathVariable Long commentId) {
        long count = reactionService.getCommentReactionCount(commentId);
        return ResponseEntity.ok(ApiResponse.success("Reaction count retrieved successfully", count));
    }
}
