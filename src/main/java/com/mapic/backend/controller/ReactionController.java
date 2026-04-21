package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.ReactionDTO;
import com.mapic.backend.entity.ReactionType;
import com.mapic.backend.service.IReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@RequiredArgsConstructor
@Slf4j
public class ReactionController {

    private final IReactionService reactionService;

    /**
     * Toggle reaction on a moment
     * POST /api/reactions/moments/{momentId}
     * Body: { "type": "LOVE" }
     */
    @PostMapping("/moments/{momentId}")
    public ResponseEntity<ApiResponse<ReactionDTO>> toggleReaction(
            @PathVariable Long momentId,
            @RequestBody ReactionRequest request) {
        
        log.info("Toggle reaction {} on moment {}", request.getType(), momentId);
        
        ReactionDTO reaction = reactionService.toggleReaction(momentId, request.getType());
        
        if (reaction == null) {
            // Reaction was removed (toggled off)
            return ResponseEntity.ok(ApiResponse.success("Reaction removed successfully", null));
        }
        
        // Reaction was added or updated
        return ResponseEntity.ok(ApiResponse.success("Reaction added successfully", reaction));
    }

    /**
     * Remove reaction from a moment
     * DELETE /api/reactions/moments/{momentId}
     */
    @DeleteMapping("/moments/{momentId}")
    public ResponseEntity<ApiResponse<Void>> removeReaction(@PathVariable Long momentId) {
        log.info("Remove reaction from moment {}", momentId);
        
        reactionService.removeReaction(momentId);
        
        return ResponseEntity.ok(ApiResponse.success("Reaction removed successfully", null));
    }

    /**
     * Get reaction count for a moment
     * GET /api/reactions/moments/{momentId}/count
     */
    @GetMapping("/moments/{momentId}/count")
    public ResponseEntity<ApiResponse<Long>> getReactionCount(@PathVariable Long momentId) {
        long count = reactionService.getReactionCount(momentId);
        
        return ResponseEntity.ok(ApiResponse.success("Reaction count retrieved successfully", count));
    }

    // Request DTO
    @lombok.Data
    public static class ReactionRequest {
        private ReactionType type;
    }
}
