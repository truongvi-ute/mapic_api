package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.response.ConversationDto;
import com.mapic.backend.dto.response.MessageDto;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    // ─── REST: Conversations ───

    @GetMapping("/rooms")
    public ResponseEntity<ApiResponse<List<ConversationDto>>> getConversations(Authentication auth) {
        User me = getUser(auth);
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", chatService.getMyConversations(me.getId())));
    }

    @PostMapping("/rooms/direct/{friendId}")
    public ResponseEntity<ApiResponse<ConversationDto>> openDirectChat(
            @PathVariable Long friendId, Authentication auth) {
        User me = getUser(auth);
        ConversationDto dto = chatService.getOrCreateDirectConversation(me.getId(), friendId);
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", dto));
    }

    @PostMapping("/rooms/group")
    public ResponseEntity<ApiResponse<ConversationDto>> createGroup(
            @RequestBody Map<String, Object> body, Authentication auth) {
        User me = getUser(auth);
        String title = (String) body.get("title");
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("memberIds");
        List<Long> memberIds = rawIds.stream().map(Long::valueOf).toList();

        ConversationDto dto = chatService.createGroupConversation(me.getId(), title, memberIds);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tạo nhóm thành công", dto));
    }

    @PostMapping("/rooms/{roomId}/members")
    public ResponseEntity<ApiResponse<ConversationDto>> addMembers(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        User me = getUser(auth);
        @SuppressWarnings("unchecked")
        List<Integer> rawIds = (List<Integer>) body.get("memberIds");
        List<Long> memberIds = rawIds.stream().map(Long::valueOf).toList();

        ConversationDto dto = chatService.addMembersToGroup(roomId, me.getId(), memberIds);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã thêm thành viên", dto));
    }

    @DeleteMapping("/rooms/{roomId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long roomId, @PathVariable Long userId, Authentication auth) {
        User me = getUser(auth);
        chatService.removeMemberFromGroup(roomId, me.getId(), userId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã xóa thành viên", null));
    }

    @PutMapping("/rooms/{roomId}/title")
    public ResponseEntity<ApiResponse<ConversationDto>> renameGroup(
            @PathVariable Long roomId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        User me = getUser(auth);
        ConversationDto dto = chatService.renameGroup(roomId, me.getId(), body.get("title"));
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã đổi tên nhóm", dto));
    }

    @PutMapping("/rooms/{roomId}/avatar")
    public ResponseEntity<ApiResponse<ConversationDto>> updateGroupAvatar(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        User me = getUser(auth);
        ConversationDto dto = chatService.updateGroupAvatar(roomId, me.getId(), file);
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã cập nhật ảnh nhóm", dto));
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(
            @PathVariable Long roomId, Authentication auth) {
        User me = getUser(auth);
        chatService.deleteGroup(roomId, me.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Đã xóa nhóm", null));
    }

    // ─── REST: Message History ───

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<List<MessageDto>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            Authentication auth) {
        User me = getUser(auth);
        List<MessageDto> messages = chatService.getMessages(roomId, me.getId(), page);
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", messages));
    }

    // ─── WebSocket: Send Messages ───

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload, Principal principal) {
        User sender = getUserByUsername(principal.getName());
        Long conversationId = Long.valueOf(payload.get("conversationId").toString());
        String type = (String) payload.get("type");
        String content = (String) payload.get("content");
        Long referenceId = payload.containsKey("referenceId") ?
                Long.valueOf(payload.get("referenceId").toString()) : null;

        if ("TEXT".equals(type)) {
            chatService.sendTextMessage(conversationId, sender.getId(), content);
        } else if ("SHARE_MOMENT".equals(type) || "SHARE_ALBUM".equals(type)) {
            chatService.sendShareMessage(conversationId, sender.getId(), type, referenceId);
        }
    }

    // ─── WebSocket: Reactions ───

    @MessageMapping("/chat.react")
    public void reactToMessage(@Payload Map<String, Object> payload, Principal principal) {
        User user = getUserByUsername(principal.getName());
        Long messageId = Long.valueOf(payload.get("messageId").toString());
        String emoji = (String) payload.get("emoji");
        chatService.toggleReaction(messageId, user.getId(), emoji);
    }

    // ─── REST: Send message via HTTP (fallback) ───

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ApiResponse<MessageDto>> sendMessageRest(
            @PathVariable Long roomId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        User me = getUser(auth);
        String type = (String) body.getOrDefault("type", "TEXT");
        String content = (String) body.get("content");
        Long referenceId = body.containsKey("referenceId") ?
                Long.valueOf(body.get("referenceId").toString()) : null;

        MessageDto dto;
        if ("TEXT".equals(type)) {
            dto = chatService.sendTextMessage(roomId, me.getId(), content);
        } else {
            dto = chatService.sendShareMessage(roomId, me.getId(), type, referenceId);
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", dto));
    }

    @PostMapping("/messages/{messageId}/react")
    public ResponseEntity<ApiResponse<MessageDto>> reactRest(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        User me = getUser(auth);
        MessageDto dto = chatService.toggleReaction(messageId, me.getId(), body.get("emoji"));
        return ResponseEntity.ok(new ApiResponse<>(true, "OK", dto));
    }

    // ─── Helpers ───

    private User getUser(Authentication auth) {
        return getUserByUsername(auth.getName());
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
