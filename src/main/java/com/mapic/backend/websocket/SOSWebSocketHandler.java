package com.mapic.backend.websocket;

import com.mapic.backend.dto.LocationUpdatePayload;
import com.mapic.backend.entity.Friendship;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.FriendshipRepository;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.SOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class SOSWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final SOSService sosService;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    @MessageMapping("/sos.location.update")
    public void handleLocationUpdate(@Payload LocationUpdatePayload payload, Authentication authentication) {
        String username = authentication.getName();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Validate that the user owns the alert they are updating
        if (!sosService.validateAlertOwnership(payload.getAlertId(), sender.getId())) {
            log.warn("User {} attempted to update location for alert {} they do not own", sender.getId(), payload.getAlertId());
            return;
        }

        // 2. Set timestamp if not present
        if (payload.getTimestamp() == null) {
            payload.setTimestamp(LocalDateTime.now());
        }

        // 3. Broadcast location to all friends
        List<Friendship> friendships = friendshipRepository.findAllFriendsByUserId(sender.getId());
        List<User> friends = friendships.stream()
                .map(f -> f.getUser1().getId().equals(sender.getId()) ? f.getUser2() : f.getUser1())
                .toList();

        for (User friend : friends) {
            messagingTemplate.convertAndSendToUser(
                    friend.getUsername(),
                    "/queue/sos.location." + payload.getAlertId(),
                    payload
            );
        }
        
        log.debug("Broadcasted SOS location update for alert {} to {} friends", payload.getAlertId(), friends.size());
    }
}
