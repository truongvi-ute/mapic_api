package com.mapic.backend.controller;

import com.mapic.backend.dto.ApiResponse;
import com.mapic.backend.dto.request.SendFriendRequestDto;
import com.mapic.backend.dto.response.FriendRequestResponse;
import com.mapic.backend.dto.response.FriendResponse;
import com.mapic.backend.dto.response.UserSearchResponse;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.UserRepository;
import com.mapic.backend.service.IFriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final IFriendService friendService;
    private final UserRepository userRepository;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
            @RequestParam String query) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<UserSearchResponse> results = friendService.searchUsers(query, currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.<List<UserSearchResponse>>builder()
                .success(true)
                .message("Found " + results.size() + " users")
                .data(results)
                .build());
    }

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> sendFriendRequest(
            @Valid @RequestBody SendFriendRequestDto dto) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        friendService.sendFriendRequest(dto, sender.getId());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Friend request sent successfully")
                .build());
    }

    @PostMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse<Void>> acceptFriendRequest(
            @PathVariable Long requestId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        friendService.acceptFriendRequest(requestId, currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Friend request accepted")
                .build());
    }

    @PostMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<Void>> rejectFriendRequest(
            @PathVariable Long requestId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        friendService.rejectFriendRequest(requestId, currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Friend request rejected")
                .build());
    }

    @DeleteMapping("/request/{requestId}")
    public ResponseEntity<ApiResponse<Void>> cancelFriendRequest(
            @PathVariable Long requestId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        friendService.cancelFriendRequest(requestId, currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Friend request cancelled")
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<FriendResponse>>> getAllFriends() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<FriendResponse> friends = friendService.getAllFriends(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<List<FriendResponse>>builder()
                .success(true)
                .message("Retrieved " + friends.size() + " friends")
                .data(friends)
                .build());
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<Void>> unfriend(
            @PathVariable Long friendId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        friendService.unfriend(friendId, currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Unfriended successfully")
                .build());
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<ApiResponse<List<FriendRequestResponse>>> getPendingRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<FriendRequestResponse> requests = friendService.getPendingRequests(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<List<FriendRequestResponse>>builder()
                .success(true)
                .message("Retrieved " + requests.size() + " pending requests")
                .data(requests)
                .build());
    }

    @GetMapping("/requests/pending/count")
    public ResponseEntity<ApiResponse<Long>> countPendingRequests() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Long count = friendService.countPendingRequests(user.getId());
        
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("Pending requests count")
                .data(count)
                .build());
    }

    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<ApiResponse<String>> getFriendshipStatus(
            @PathVariable Long targetUserId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String status = friendService.getFriendshipStatus(currentUser.getId(), targetUserId);
        
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Friendship status")
                .data(status)
                .build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserSearchResponse>> getUserById(
            @PathVariable Long userId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserSearchResponse user = friendService.getUserById(userId, currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.<UserSearchResponse>builder()
                .success(true)
                .message("User found")
                .data(user)
                .build());
    }
}
