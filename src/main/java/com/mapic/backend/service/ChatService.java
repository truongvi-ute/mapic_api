package com.mapic.backend.service;

import com.mapic.backend.dto.response.ConversationDto;
import com.mapic.backend.dto.response.MessageDto;
import com.mapic.backend.dto.response.ParticipantDto;
import com.mapic.backend.entity.*;
import com.mapic.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageReactionRepository messageReactionRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final MomentRepository momentRepository;
    private final AlbumRepository albumRepository;
    private final AlbumItemRepository albumItemRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final INotificationService notificationService;

    // ─────────────────── Conversations ───────────────────

    @Transactional(readOnly = true)
    public List<ConversationDto> getMyConversations(Long userId) {
        User user = getUser(userId);
        List<Conversation> convos = conversationRepository.findAllByUser(user);
        return convos.stream()
                .map(c -> toConversationDto(c, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public ConversationDto getOrCreateDirectConversation(Long myId, Long friendId) {
        User me = getUser(myId);
        User friend = getUser(friendId);

        // Validate friendship
        if (!friendshipRepository.existsFriendshipBetweenUsers(myId, friendId)) {
            throw new RuntimeException("Bạn chỉ có thể nhắn tin với bạn bè");
        }

        // Return existing if already exists
        List<Conversation> existing = conversationRepository.findDirectConversation(me, friend);
        if (!existing.isEmpty()) {
            return toConversationDto(existing.get(0), myId);
        }

        // Create new direct conv
        Conversation conv = Conversation.builder()
                .isGroup(false)
                .creator(me)
                .build();
        conv = conversationRepository.save(conv);

        participantRepository.save(Participant.builder().conversation(conv).user(me).role("ADMIN").build());
        participantRepository.save(Participant.builder().conversation(conv).user(friend).role("MEMBER").build());

        return toConversationDto(conv, myId);
    }

    @Transactional
    public ConversationDto createGroupConversation(Long creatorId, String title, List<Long> memberIds) {
        User creator = getUser(creatorId);

        if (memberIds == null || memberIds.size() < 2) {
            throw new RuntimeException("Nhóm cần ít nhất 3 người (bạn + 2 thành viên)");
        }

        // Validate all members are friends
        for (Long memberId : memberIds) {
            if (!friendshipRepository.existsFriendshipBetweenUsers(creatorId, memberId)) {
                User m = getUser(memberId);
                throw new RuntimeException("Người dùng " + m.getUsername() + " chưa là bạn bè của bạn");
            }
        }

        Conversation conv = Conversation.builder()
                .isGroup(true)
                .title(title)
                .creator(creator)
                .build();
        conv = conversationRepository.save(conv);

        // Add creator as ADMIN
        participantRepository.save(Participant.builder().conversation(conv).user(creator).role("ADMIN").build());

        // Add members
        for (Long memberId : memberIds) {
            User member = getUser(memberId);
            participantRepository.save(Participant.builder().conversation(conv).user(member).role("MEMBER").build());
        }

        return toConversationDto(conv, creatorId);
    }

    @Transactional
    public ConversationDto addMembersToGroup(Long conversationId, Long requesterId, List<Long> memberIds) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

        if (!conv.getIsGroup()) {
            throw new RuntimeException("Chỉ có thể thêm thành viên vào nhóm");
        }

        if (!conv.getCreator().getId().equals(requesterId)) {
            throw new RuntimeException("Chỉ trưởng nhóm mới có thể thêm thành viên");
        }

        if (memberIds == null || memberIds.isEmpty()) {
            throw new RuntimeException("Danh sách thành viên không được rỗng");
        }

        // Validate all members are friends with creator
        for (Long memberId : memberIds) {
            if (!friendshipRepository.existsFriendshipBetweenUsers(requesterId, memberId)) {
                User m = getUser(memberId);
                throw new RuntimeException("Người dùng " + m.getUsername() + " chưa là bạn bè của bạn");
            }

            // Check if already a member
            if (participantRepository.existsByConversationIdAndUserId(conversationId, memberId)) {
                User m = getUser(memberId);
                throw new RuntimeException("Người dùng " + m.getUsername() + " đã là thành viên của nhóm");
            }
        }

        // Add members
        for (Long memberId : memberIds) {
            User member = getUser(memberId);
            participantRepository.save(Participant.builder()
                    .conversation(conv)
                    .user(member)
                    .role("MEMBER")
                    .build());
        }

        return toConversationDto(conv, requesterId);
    }

    @Transactional
    public void removeMemberFromGroup(Long conversationId, Long requesterId, Long targetUserId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

        if (!conv.getIsGroup()) {
            throw new RuntimeException("Chỉ có thể xóa thành viên trong nhóm");
        }

        if (!conv.getCreator().getId().equals(requesterId)) {
            throw new RuntimeException("Chỉ người tạo nhóm mới có thể xóa thành viên");
        }

        // Đảm bảo nhóm còn ít nhất 3 người sau khi xóa
        long currentCount = participantRepository.findByConversation(conv).size();
        if (currentCount <= 3) {
            throw new RuntimeException("Nhóm cần ít nhất 3 người, không thể xóa thêm thành viên");
        }

        User target = getUser(targetUserId);
        participantRepository.deleteByConversationAndUser(conv, target);
    }

    @Transactional
    public ConversationDto renameGroup(Long conversationId, Long requesterId, String newTitle) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

        if (!conv.getIsGroup()) {
            throw new RuntimeException("Chỉ có thể đổi tên nhóm");
        }
        if (!conv.getCreator().getId().equals(requesterId)) {
            throw new RuntimeException("Chỉ trưởng nhóm mới có thể đổi tên");
        }
        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new RuntimeException("Tên nhóm không được để trống");
        }

        conv.setTitle(newTitle.trim());
        conversationRepository.save(conv);
        return toConversationDto(conv, requesterId);
    }

    @Transactional
    public ConversationDto updateGroupAvatar(Long conversationId, Long requesterId, org.springframework.web.multipart.MultipartFile file) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

        if (!conv.getIsGroup()) {
            throw new RuntimeException("Chỉ có thể cập nhật ảnh cho nhóm");
        }
        if (!conv.getCreator().getId().equals(requesterId)) {
            throw new RuntimeException("Chỉ trưởng nhóm mới có thể cập nhật ảnh");
        }

        // Delete old avatar if exists
        if (conv.getGroupAvatarUrl() != null && !conv.getGroupAvatarUrl().isEmpty()) {
            try {
                // Assuming you have a storage service
                // storageService.delete(conv.getGroupAvatarUrl(), "group-avatars");
            } catch (Exception e) {
                // Log error but continue
            }
        }

        // Upload new avatar
        // String filename = storageService.store(file, "group-avatars");
        // For now, just use a placeholder or implement storage service
        String filename = "group-avatar-" + conversationId + "-" + System.currentTimeMillis() + ".jpg";
        
        conv.setGroupAvatarUrl(filename);
        conversationRepository.save(conv);
        return toConversationDto(conv, requesterId);
    }

    @Transactional
    public void deleteGroup(Long conversationId, Long requesterId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

        if (!conv.getIsGroup()) {
            throw new RuntimeException("Chỉ có thể xóa nhóm");
        }
        if (!conv.getCreator().getId().equals(requesterId)) {
            throw new RuntimeException("Chỉ trưởng nhóm mới có thể xóa nhóm");
        }

        // Xóa reactions → messages → participants → conversation
        List<Message> messages = messageRepository.findAllByConversation(conv);
        for (Message msg : messages) {
            messageReactionRepository.deleteByMessage(msg);
        }
        messageRepository.deleteAll(messages);
        participantRepository.deleteByConversation(conv);
        conversationRepository.delete(conv);
    }

    // ─────────────────── Messages ───────────────────

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(Long conversationId, Long userId, int page) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

        if (!participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new RuntimeException("Bạn không phải thành viên của cuộc trò chuyện này");
        }

        return messageRepository.findByConversationOrderByCreatedAtDesc(
                        conv, PageRequest.of(page, 30, Sort.by("createdAt").descending()))
                .getContent()
                .stream()
                .map(m -> toMessageDto(m, userId))
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto sendTextMessage(Long conversationId, Long senderId, String content) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

        User sender = getUser(senderId);
        if (!participantRepository.existsByConversationIdAndUserId(conversationId, senderId)) {
            throw new RuntimeException("Bạn không phải thành viên của cuộc trò chuyện này");
        }

        TextMessage msg = new TextMessage();
        msg.setConversation(conv);
        msg.setSender(sender);
        msg.setType(MessageType.TEXT);
        msg.setContent(content);
        messageRepository.save(msg);

        // Update last message
        conv.setLastMessage(msg);
        conversationRepository.save(conv);

        MessageDto dto = toMessageDto(msg, senderId);

        // Broadcast to all participants (message topic)
        broadcastToConversation(conv, dto);

        // Notify all participants about list update (conversation topic)
        broadcastConversationUpdate(conv);

        // Send individual notifications to participants
        notifyParticipants(conv, sender, MessageType.TEXT, msg.getId());

        return dto;
    }

    @Transactional
    public MessageDto sendShareMessage(Long conversationId, Long senderId, String shareType, Long referenceId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Cuộc trò chuyện không tồn tại"));

        User sender = getUser(senderId);
        if (!participantRepository.existsByConversationIdAndUserId(conversationId, senderId)) {
            throw new RuntimeException("Bạn không phải thành viên của cuộc trò chuyện này");
        }

        ShareMessage msg = new ShareMessage();
        msg.setConversation(conv);
        msg.setSender(sender);
        msg.setType(MessageType.SHARE);
        msg.setShareType(shareType);
        msg.setTargetId(referenceId);
        messageRepository.save(msg);

        conv.setLastMessage(msg);
        conversationRepository.save(conv);

        MessageDto dto = toMessageDto(msg, senderId);
        broadcastToConversation(conv, dto);
        broadcastConversationUpdate(conv);

        // Send individual notifications
        notifyParticipants(conv, sender, MessageType.SHARE, msg.getId());

        return dto;
    }

    @Transactional
    public MessageDto toggleReaction(Long messageId, Long userId, String emoji) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));
        User user = getUser(userId);

        Optional<MessageReaction> existing = messageReactionRepository.findByMessageAndUser(msg, user);
        if (existing.isPresent()) {
            if (existing.get().getEmoji().equals(emoji)) {
                // Same emoji -> remove
                messageReactionRepository.delete(existing.get());
            } else {
                // Different emoji -> update
                existing.get().setEmoji(emoji);
                messageReactionRepository.save(existing.get());
            }
        } else {
            messageReactionRepository.save(MessageReaction.builder()
                    .message(msg)
                    .user(user)
                    .emoji(emoji)
                    .build());
        }

        MessageDto dto = toMessageDto(msg, userId);

        // Broadcast updated message reactions
        messagingTemplate.convertAndSend(
                "/topic/chat/" + msg.getConversation().getId() + "/reactions",
                dto);

        return dto;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getReactionDetails(Long messageId) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Tin nhắn không tồn tại"));

        List<MessageReaction> reactions = messageReactionRepository.findByMessage(msg);
        
        return reactions.stream()
                .map(r -> {
                    Map<String, Object> detail = new java.util.HashMap<>();
                    detail.put("userId", r.getUser().getId());
                    detail.put("username", r.getUser().getUsername());
                    detail.put("fullName", r.getUser().getName());
                    detail.put("avatarUrl", r.getUser().getUserProfile() != null 
                            ? r.getUser().getUserProfile().getAvatarUrl() : null);
                    detail.put("emoji", r.getEmoji());
                    return detail;
                })
                .collect(Collectors.toList());
    }

    // ─────────────────── Helpers ───────────────────

    private void broadcastToConversation(Conversation conv, MessageDto dto) {
        messagingTemplate.convertAndSend("/topic/chat/" + conv.getId(), dto);
    }

    private void broadcastConversationUpdate(Conversation conv) {
        List<Participant> participants = participantRepository.findByConversation(conv);
        for (Participant p : participants) {
            // For each participant, send the DTO tailored to them (correct myReaction etc)
            ConversationDto convoDto = toConversationDto(conv, p.getUser().getId());
            messagingTemplate.convertAndSendToUser(
                p.getUser().getUsername(),
                "/topic/conversations",
                convoDto
            );
        }
    }

    private void notifyParticipants(Conversation conv, User sender, MessageType type, Long messageId) {
        List<Participant> participants = participantRepository.findByConversation(conv);
        for (Participant p : participants) {
            if (!p.getUser().getId().equals(sender.getId())) {
                notificationService.createNotification(
                    sender, 
                    p.getUser(), 
                    NotificationType.NEW_MESSAGE, 
                    "MESSAGE", 
                    messageId
                );
            }
        }
    }

    private MessageDto toMessageDto(Message msg, Long currentUserId) {
        // Unproxy để đảm bảo instanceof hoạt động đúng với JOINED inheritance
        Message realMsg = (Message) Hibernate.unproxy(msg);

        // Count reactions grouped by emoji
        List<MessageReaction> reactions = messageReactionRepository.findByMessage(msg);
        Map<String, Long> reactionCounts = reactions.stream()
                .collect(Collectors.groupingBy(MessageReaction::getEmoji, Collectors.counting()));

        String myReaction = reactions.stream()
                .filter(r -> r.getUser().getId().equals(currentUserId))
                .map(MessageReaction::getEmoji)
                .findFirst()
                .orElse(null);

        String avatarUrl = null;
        String senderName = msg.getSender().getName(); // Get full name
        if (msg.getSender().getUserProfile() != null) {
            avatarUrl = msg.getSender().getUserProfile().getAvatarUrl();
        }

        MessageDto.MessageDtoBuilder builder = MessageDto.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSender().getId())
                .senderUsername(msg.getSender().getUsername())
                .senderName(senderName) // Add full name
                .senderAvatarUrl(avatarUrl)
                .type(msg.getType().name())
                .reactions(reactionCounts)
                .myReaction(myReaction)
                .createdAt(msg.getCreatedAt());

        if (realMsg instanceof TextMessage textMsg) {
            builder.content(textMsg.getContent());
        } else if (realMsg instanceof ShareMessage shareMsg) {
            builder.referenceId(shareMsg.getTargetId())
                   .content(shareMsg.getShareType());

            // Add snippet for sharing preview
            if ("MOMENT".equals(shareMsg.getShareType()) || "SHARE_MOMENT".equals(shareMsg.getShareType())) {
                momentRepository.findById(shareMsg.getTargetId()).ifPresent(m -> {
                    String firstImage = (!m.getMedia().isEmpty()) ? m.getMedia().get(0).getMediaUrl() : null;
                    builder.sharedPreview(Map.of(
                        "id", m.getId(),
                        "content", m.getContent() != null ? m.getContent() : "",
                        "imageUrl", firstImage != null ? firstImage : "",
                        "authorName", m.getAuthor().getName()
                    ));
                });
            } else if ("ALBUM".equals(shareMsg.getShareType()) || "SHARE_ALBUM".equals(shareMsg.getShareType())) {
                albumRepository.findById(shareMsg.getTargetId()).ifPresent(a -> {
                    builder.sharedPreview(Map.of(
                        "id", a.getId(),
                        "title", a.getTitle(),
                        "description", a.getDescription() != null ? a.getDescription() : "",
                        "itemCount", albumItemRepository.countByAlbum(a)
                    ));
                });
            }
        }

        return builder.build();
    }

    private ConversationDto toConversationDto(Conversation conv, Long userId) {
        List<Participant> participants = participantRepository.findByConversation(conv);
        List<ParticipantDto> participantDtos = participants.stream()
                .map(p -> {
                    String fullName = p.getUser().getName();
                    String avatar = p.getUser().getUserProfile() != null
                            ? p.getUser().getUserProfile().getAvatarUrl() : null;
                    String cover = p.getUser().getUserProfile() != null
                            ? p.getUser().getUserProfile().getCoverImageUrl() : null;
                    return ParticipantDto.builder()
                            .userId(p.getUser().getId())
                            .username(p.getUser().getUsername())
                            .fullName(fullName)
                            .avatarUrl(avatar)
                            .coverImageUrl(cover)
                            .role(p.getRole())
                            .build();
                })
                .collect(Collectors.toList());

        MessageDto lastMsgDto = null;
        if (conv.getLastMessage() != null) {
            lastMsgDto = toMessageDto(conv.getLastMessage(), userId);
        }

        return ConversationDto.builder()
                .id(conv.getId())
                .isGroup(conv.getIsGroup())
                .title(conv.getTitle())
                .groupAvatarUrl(conv.getGroupAvatarUrl()) // Add group avatar
                .creatorId(conv.getCreator() != null ? conv.getCreator().getId() : null)
                .createdAt(conv.getCreatedAt())
                .lastMessage(lastMsgDto)
                .participants(participantDtos)
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
    }
}
