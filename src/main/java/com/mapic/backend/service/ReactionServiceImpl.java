package com.mapic.backend.service;

import com.mapic.backend.dto.ReactionDTO;
import com.mapic.backend.entity.Moment;
import com.mapic.backend.entity.Reaction;
import com.mapic.backend.entity.ReactionType;
import com.mapic.backend.entity.User;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.exception.ValidationException;
import com.mapic.backend.repository.MomentRepository;
import com.mapic.backend.repository.ReactionRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReactionServiceImpl implements IReactionService {

    private final ReactionRepository reactionRepository;
    private final MomentRepository momentRepository;
    private final UserRepository userRepository;

    // Moment chỉ cho phép HEART
    private static final Set<ReactionType> ALLOWED_MOMENT_REACTIONS = Set.of(ReactionType.HEART);
    
    // Comment & Message cho phép 6 loại
    private static final Set<ReactionType> ALLOWED_COMMENT_MESSAGE_REACTIONS = 
        Set.of(ReactionType.LIKE, ReactionType.HEART, ReactionType.HAHA, 
               ReactionType.WOW, ReactionType.SAD, ReactionType.ANGRY);

    @Override
    @Transactional
    public ReactionDTO toggleReaction(Long momentId, ReactionType type) {
        // Validate reaction type for moment
        if (!ALLOWED_MOMENT_REACTIONS.contains(type)) {
            throw new ValidationException(
                String.format("Reaction type %s is not allowed for moments. Only HEART is supported.", type)
            );
        }

        // Get current user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Get moment
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new NotFoundException("Moment not found with id: " + momentId));

        // Check if user already reacted
        var existingReaction = reactionRepository.findByUserAndMoment(user, moment);

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            
            // If same type, remove reaction (toggle off)
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
                log.info("Removed reaction {} from moment {} by user {}", type, momentId, username);
                return null;
            }
            
            // If different type, update reaction
            reaction.setType(type);
            Reaction updated = reactionRepository.save(reaction);
            log.info("Updated reaction to {} on moment {} by user {}", type, momentId, username);
            return mapToDTO(updated);
        }

        // Create new reaction
        Reaction newReaction = Reaction.builder()
                .user(user)
                .moment(moment)
                .type(type)
                .build();

        Reaction saved = reactionRepository.save(newReaction);
        log.info("Added reaction {} to moment {} by user {}", type, momentId, username);
        
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void removeReaction(Long momentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new NotFoundException("Moment not found with id: " + momentId));

        reactionRepository.deleteByUserAndMoment(user, moment);
        log.info("Removed reaction from moment {} by user {}", momentId, username);
    }

    @Override
    public long getReactionCount(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new NotFoundException("Moment not found with id: " + momentId));
        
        return reactionRepository.countByMoment(moment);
    }

    private ReactionDTO mapToDTO(Reaction reaction) {
        return ReactionDTO.builder()
                .id(reaction.getId())
                .userId(reaction.getUser().getId())
                .username(reaction.getUser().getUsername())
                .momentId(reaction.getMoment().getId())
                .type(reaction.getType())
                .createdAt(reaction.getCreatedAt())
                .build();
    }
}
