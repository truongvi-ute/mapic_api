package com.mapic.backend.service;

import com.mapic.backend.dto.ReactionDTO;
import com.mapic.backend.entity.Comment;
import com.mapic.backend.entity.Moment;
import com.mapic.backend.entity.Reaction;
import com.mapic.backend.entity.ReactionType;
import com.mapic.backend.entity.User;
import com.mapic.backend.entity.NotificationType;
import com.mapic.backend.exception.NotFoundException;
import com.mapic.backend.exception.ValidationException;
import com.mapic.backend.repository.CommentRepository;
import com.mapic.backend.repository.MomentRepository;
import com.mapic.backend.repository.ReactionRepository;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.INotificationService;
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
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final INotificationService notificationService;

    // Moment cho phép 6 loại emoji (giống comment)
    private static final Set<ReactionType> ALLOWED_MOMENT_REACTIONS = 
        Set.of(ReactionType.LIKE, ReactionType.HEART, ReactionType.HAHA, 
               ReactionType.WOW, ReactionType.SAD, ReactionType.ANGRY);
    
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
                String.format("Reaction type %s is not allowed for moments.", type)
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

        // Notify moment author
        notificationService.createNotification(user, moment.getAuthor(), NotificationType.MOMENT_REACTION, "MOMENT", moment.getId());
        
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

    @Override
    @Transactional
    public ReactionDTO toggleCommentReaction(Long commentId, ReactionType type) {
        if (!ALLOWED_COMMENT_MESSAGE_REACTIONS.contains(type)) {
            throw new ValidationException("Reaction type not allowed for comments.");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        var existingReaction = reactionRepository.findByUserAndComment(user, comment);

        if (existingReaction.isPresent()) {
            Reaction reaction = existingReaction.get();
            if (reaction.getType() == type) {
                reactionRepository.delete(reaction);
                log.info("Removed reaction {} from comment {} by user {}", type, commentId, username);
                return null;
            }
            reaction.setType(type);
            Reaction updated = reactionRepository.save(reaction);
            log.info("Updated reaction to {} on comment {} by user {}", type, commentId, username);
            return mapToDTO(updated);
        }

        Reaction newReaction = Reaction.builder()
                .user(user)
                .comment(comment)
                .type(type)
                .build();

        Reaction saved = reactionRepository.save(newReaction);
        log.info("Added reaction {} to comment {} by user {}", type, commentId, username);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void removeCommentReaction(Long commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));

        reactionRepository.deleteByUserAndComment(user, comment);
        log.info("Removed reaction from comment {} by user {}", commentId, username);
    }

    @Override
    public long getCommentReactionCount(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found with id: " + commentId));
        return reactionRepository.countByComment(comment);
    }

    private ReactionDTO mapToDTO(Reaction reaction) {
        return ReactionDTO.builder()
                .id(reaction.getId())
                .userId(reaction.getUser().getId())
                .username(reaction.getUser().getUsername())
                .momentId(reaction.getMoment() != null ? reaction.getMoment().getId() : null)
                // Add commentId support later if ReactionDTO is updated, but currently we just avoid null pointer
                .type(reaction.getType())
                .createdAt(reaction.getCreatedAt())
                .build();
    }
}
