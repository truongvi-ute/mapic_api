package com.mapic.backend.service;

import com.mapic.backend.dto.ReactionDTO;
import com.mapic.backend.entity.ReactionType;

public interface IReactionService {
    
    /**
     * Toggle reaction on a moment
     * - If user hasn't reacted: Add reaction
     * - If user reacted with same type: Remove reaction
     * - If user reacted with different type: Update reaction
     * 
     * For moments: Only LOVE is allowed
     */
    ReactionDTO toggleReaction(Long momentId, ReactionType type);
    
    /**
     * Remove reaction from a moment
     */
    void removeReaction(Long momentId);
    
    /**
     * Get reaction count for a moment
     */
    long getReactionCount(Long momentId);
}
