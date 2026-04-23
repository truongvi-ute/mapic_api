package com.mapic.backend.service;

import com.mapic.backend.dto.CommentDto;
import com.mapic.backend.dto.request.CommentRequest;
import com.mapic.backend.entity.Comment;
import com.mapic.backend.entity.Moment;
import com.mapic.backend.entity.User;
import com.mapic.backend.entity.Reaction;
import com.mapic.backend.entity.ReactionType;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements ICommentService {

    private final CommentRepository commentRepository;
    private final MomentRepository momentRepository;
    private final UserRepository userRepository;
    private final ReactionRepository reactionRepository;
    private final INotificationService notificationService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
    
    private User getCurrentUserOrNull() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @Transactional
    public CommentDto createComment(Long momentId, CommentRequest request) {
        User user = getCurrentUser();
        
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new NotFoundException("Moment not found"));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
            
            // Only allow 1 level of nesting
            if (parentComment.getParentComment() != null) {
                // If trying to reply to a reply, make it a reply to the top level comment instead
                parentComment = parentComment.getParentComment();
            }
            
            if (!parentComment.getMoment().getId().equals(momentId)) {
                throw new ValidationException("Comment and parent comment must belong to the same moment");
            }
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(user)
                .moment(moment)
                .parentComment(parentComment)
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("User {} created comment {} on moment {}", user.getUsername(), savedComment.getId(), momentId);

        // Notify moment author
        notificationService.createNotification(user, moment.getAuthor(), NotificationType.MOMENT_COMMENT, "MOMENT", momentId);

        // If it's a reply, notify the parent comment author
        if (parentComment != null) {
            notificationService.createNotification(user, parentComment.getAuthor(), NotificationType.MOMENT_COMMENT, "COMMENT", parentComment.getId());
        }
        
        return convertToDto(savedComment, user.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByMoment(Long momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new NotFoundException("Moment not found"));
        
        User currentUser = getCurrentUserOrNull();
        Long currentUserId = currentUser != null ? currentUser.getId() : null;

        // Get only top-level comments
        Page<Comment> topLevelComments = commentRepository.findByMomentAndParentCommentIsNullOrderByCreatedAtDesc(
                moment, PageRequest.of(0, 100)); // Get up to 100 top level comments for now

        return topLevelComments.getContent().stream()
                .map(comment -> convertToDtoWithReplies(comment, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        User user = getCurrentUser();
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (!comment.getAuthor().getId().equals(user.getId()) && !comment.getMoment().getAuthor().getId().equals(user.getId())) {
            throw new ValidationException("Not authorized to delete this comment");
        }

        // If it's a top level comment, delete replies first
        if (comment.getParentComment() == null) {
            List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
            for (Comment reply : replies) {
                // Delete reactions for reply
                reactionRepository.deleteByComment(reply);
                commentRepository.delete(reply);
            }
        }
        
        // Delete reactions for the comment itself
        reactionRepository.deleteByComment(comment);
        
        commentRepository.delete(comment);
    }

    private CommentDto convertToDtoWithReplies(Comment comment, Long currentUserId) {
        CommentDto dto = convertToDto(comment, currentUserId);
        
        // Fetch replies
        List<Comment> replies = commentRepository.findByParentCommentOrderByCreatedAtAsc(comment);
        List<CommentDto> replyDtos = replies.stream()
                .map(reply -> convertToDto(reply, currentUserId))
                .collect(Collectors.toList());
                
        dto.setReplies(replyDtos);
        return dto;
    }

    private CommentDto convertToDto(Comment comment, Long currentUserId) {
        long reactionCount = reactionRepository.countByComment(comment);
        
        boolean userReacted = false;
        ReactionType userReactionType = null;
        if (currentUserId != null) {
            User currentUser = new User();
            currentUser.setId(currentUserId);
            
            // Tìm reaction của user hiện tại
            Optional<Reaction> userReaction = reactionRepository.findByUserAndComment(currentUser, comment);
            if (userReaction.isPresent()) {
                userReacted = true;
                userReactionType = userReaction.get().getType();
            }
        }

        CommentDto.AuthorDto authorDto = CommentDto.AuthorDto.builder()
                .id(comment.getAuthor().getId())
                .username(comment.getAuthor().getUsername())
                .fullName(comment.getAuthor().getName())
                .avatarUrl(comment.getAuthor().getUserProfile() != null 
                        ? comment.getAuthor().getUserProfile().getAvatarUrl() : null)
                .build();

        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(authorDto)
                .momentId(comment.getMoment().getId())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .createdAt(comment.getCreatedAt())
                .reactionCount(reactionCount)
                .userReacted(userReacted)
                .userReactionType(userReactionType)
                .build();
    }
}
