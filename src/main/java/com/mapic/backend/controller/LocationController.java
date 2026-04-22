package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.response.FriendLocationDTO;
import com.mapic.backend.entity.Friendship;
import com.mapic.backend.entity.User;
import com.mapic.backend.entity.UserStatus;
import com.mapic.backend.repository.FriendshipRepository;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.repository.UserStatusRepository;
import com.mapic.backend.service.LiveLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LiveLocationService liveLocationService;
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public record LocationPayload(double longitude, double latitude) {
    }

    // ─────────────────────────────────────────────────────────────────
    // WebSocket: receives live location updates from clients
    // ─────────────────────────────────────────────────────────────────

    @MessageMapping("/location.update")
    public void processLocationUpdate(@Payload LocationPayload location, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsernameOrEmail(username, username).orElseThrow();

        // 1. Persist last location + timestamp in UserStatus (for offline view)
        persistLastLocation(user, location.longitude(), location.latitude());

        // 2. Update ephemeral Redis geo-index (for radius queries)
        try {
            liveLocationService.updateUserLocation(user.getId(), location.longitude(), location.latitude());
        } catch (Exception e) {
            log.warn("[LocationAPI] Failed to update Redis location for user {}: {}", user.getId(), e.getMessage());
        }

        // 3. Check if this user has location sharing enabled before broadcasting
        UserStatus status = userStatusRepository.findById(user.getId()).orElse(null);
        if (status != null && Boolean.FALSE.equals(status.getIsSharingLocation())) {
            log.debug("User {} has location sharing disabled — skipping broadcast", user.getId());
            return;
        }

        // 4. Fetch friend list
        List<Friendship> friendships = friendshipRepository.findAllFriendsByUserId(user.getId());
        List<Long> friendIds = friendships.stream()
                .map(f -> f.getUser1().getId().equals(user.getId()) ? f.getUser2().getId() : f.getUser1().getId())
                .toList();

        // 5. Prepare broadcast payload
        LiveLocationService.UserLocationDTO broadcastMessage = new LiveLocationService.UserLocationDTO(
                user.getId(),
                location.longitude(),
                location.latitude());

        // 6. Fire to each friend's personal queue
        for (Long friendId : friendIds) {
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(friendId),
                    "/queue/location",
                    broadcastMessage);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // REST: GET /api/location/friends — last known positions of friends
    // Returns ALL friends who have isSharingLocation = true,
    // even when they are offline, using the DB-persisted lastLat/Lng.
    // Also marks each friend as "online" if they have a live
    // Redis entry (i.e., they pushed an update recently).
    // ─────────────────────────────────────────────────────────────────

    @GetMapping("/api/location/friends")
    @ResponseBody
    public ResponseEntity<ApiResponse<List<FriendLocationDTO>>> getFriendsLocations(Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsernameOrEmail(username, username).orElseThrow();

        List<Friendship> friendships = friendshipRepository.findAllFriendsByUserId(currentUser.getId());
        log.info("[LocationAPI] Found {} friendships for user {}", friendships.size(), username);
        List<Long> friendIds = friendships.stream()
                .map(f -> f.getUser1().getId().equals(currentUser.getId())
                        ? f.getUser2().getId()
                        : f.getUser1().getId())
                .collect(Collectors.toList());

        if (friendIds.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No friends", List.of()));
        }

        // Fetch live Redis positions to determine who is "online"
        Set<Long> onlineUserIds = Set.of();
        try {
            List<LiveLocationService.UserLocationDTO> liveLocations = liveLocationService
                    .getFriendsLocations(friendIds);
            onlineUserIds = liveLocations.stream()
                    .map(LiveLocationService.UserLocationDTO::userId)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("[LocationAPI] Redis is unavailable — falling back to DB-only offline mode: {}", e.getMessage());
        }

        // Build result from friendships + UserStatus rows
        List<FriendLocationDTO> result = new ArrayList<>();
        for (Friendship friendship : friendships) {
            User friend = friendship.getUser1().getId().equals(currentUser.getId())
                    ? friendship.getUser2()
                    : friendship.getUser1();

            // Look up UserStatus for last known location + sharing flag
            UserStatus status = userStatusRepository.findById(friend.getId()).orElse(null);

            // Skip friends who have explicitly disabled sharing
            if (status != null && Boolean.FALSE.equals(status.getIsSharingLocation())) {
                continue;
            }

            String avatarUrl = friend.getUserProfile() != null
                    ? friend.getUserProfile().getAvatarUrl()
                    : null;
            
            LocalDateTime profileUpdatedAt = friend.getUserProfile() != null
                    ? friend.getUserProfile().getUpdatedAt()
                    : null;

            Double lat = status != null ? status.getLastLat() : null;
            Double lng = status != null ? status.getLastLng() : null;
            LocalDateTime lastSeenAt = status != null ? status.getLastSeenAt() : null;

            // Only include if we actually have a location to show
            if (lat == null || lng == null) {
                log.warn("[LocationAPI] Skipping friend {} - no valid coordinates in UserStatus", friend.getUsername());
                continue;
            }

            result.add(FriendLocationDTO.builder()
                    .userId(friend.getId())
                    .name(friend.getName())
                    .username(friend.getUsername())
                    .avatarUrl(avatarUrl)
                    .latitude(lat)
                    .longitude(lng)
                    .lastSeenAt(lastSeenAt)
                    .isOnline(onlineUserIds.contains(friend.getId()))
                    .profileUpdatedAt(profileUpdatedAt)
                    .build());
        }

        return ResponseEntity.ok(ApiResponse.success("Friends locations retrieved", result));
    }

    // ─────────────────────────────────────────────────────────────────
    // REST: PUT /api/location/sharing?enabled=true|false
    // Allows a user to opt in/out of location sharing.
    // ─────────────────────────────────────────────────────────────────

    @PutMapping("/api/location/sharing")
    @ResponseBody
    public ResponseEntity<ApiResponse<String>> toggleLocationSharing(
            @RequestParam boolean enabled,
            Authentication authentication) {

        String username = authentication.getName();
        User user = userRepository.findByUsernameOrEmail(username, username).orElseThrow();

        UserStatus status = userStatusRepository.findById(user.getId())
                .orElseGet(() -> UserStatus.builder()
                        .user(user)
                        .isSharingLocation(true)
                        .build());

        status.setIsSharingLocation(enabled);
        userStatusRepository.save(status);

        log.info("User {} set location sharing to {}", user.getId(), enabled);
        String msg = enabled ? "Đang chia sẻ vị trí" : "Đã tắt chia sẻ vị trí";
        return ResponseEntity.ok(ApiResponse.success(msg, enabled ? "SHARING" : "HIDDEN"));
    }

    // ─────────────────────────────────────────────────────────────────
    // REST: GET /api/location/sharing/status — get my own sharing flag
    // ─────────────────────────────────────────────────────────────────

    @GetMapping("/api/location/sharing/status")
    @ResponseBody
    public ResponseEntity<ApiResponse<Boolean>> getSharingStatus(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsernameOrEmail(username, username).orElseThrow();

        UserStatus status = userStatusRepository.findById(user.getId()).orElse(null);
        boolean sharing = status == null || Boolean.TRUE.equals(status.getIsSharingLocation()); // default true
        return ResponseEntity.ok(ApiResponse.success("Sharing status", sharing));
    }

    // ─────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────

    private void persistLastLocation(User user, double longitude, double latitude) {
        UserStatus status = userStatusRepository.findById(user.getId())
                .orElseGet(() -> UserStatus.builder()
                        .user(user)
                        .isSharingLocation(true)
                        .build());

        status.setLastLng(longitude);
        status.setLastLat(latitude);
        status.setLastSeenAt(LocalDateTime.now());
        userStatusRepository.save(status);
    }
}
