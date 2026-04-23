package com.mapic.backend.repository;

import com.mapic.backend.entity.FriendRequest;
import com.mapic.backend.entity.FriendRequestStatus;
import com.mapic.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    // Check if request exists between two users
    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);
    
    // Check if request exists between two users with specific status
    Optional<FriendRequest> findBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);
    
    // Find pending requests received by user
    @Query("SELECT fr FROM FriendRequest fr " +
           "JOIN FETCH fr.sender s " +
           "LEFT JOIN FETCH s.userProfile " +
           "WHERE fr.receiver.id = :userId " +
           "AND fr.status = 'PENDING' " +
           "AND fr.expiresAt > :now " +
           "ORDER BY fr.createdAt DESC")
    List<FriendRequest> findPendingRequestsByReceiver(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // Count pending requests
    @Query("SELECT COUNT(fr) FROM FriendRequest fr " +
           "WHERE fr.receiver.id = :userId " +
           "AND fr.status = 'PENDING' " +
           "AND fr.expiresAt > :now")
    Long countPendingRequestsByReceiver(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // Find expired requests for cleanup
    List<FriendRequest> findByStatusAndExpiresAtBefore(FriendRequestStatus status, LocalDateTime dateTime);
    
    // Check if request exists (any direction, pending and not expired)
    @Query("SELECT fr FROM FriendRequest fr " +
           "WHERE ((fr.sender.id = :userId1 AND fr.receiver.id = :userId2) " +
           "OR (fr.sender.id = :userId2 AND fr.receiver.id = :userId1)) " +
           "AND fr.status = 'PENDING' " +
           "AND fr.expiresAt > :now")
    Optional<FriendRequest> findPendingRequestBetweenUsers(
            @Param("userId1") Long userId1, 
            @Param("userId2") Long userId2,
            @Param("now") LocalDateTime now
    );
}
