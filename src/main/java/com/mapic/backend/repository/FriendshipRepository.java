package com.mapic.backend.repository;

import com.mapic.backend.entity.Friendship;
import com.mapic.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    
    // Find all friends of a user
    @Query("SELECT f FROM Friendship f " +
           "LEFT JOIN FETCH f.user1 u1 " +
           "LEFT JOIN FETCH u1.userProfile " +
           "LEFT JOIN FETCH f.user2 u2 " +
           "LEFT JOIN FETCH u2.userProfile " +
           "WHERE f.user1.id = :userId OR f.user2.id = :userId " +
           "ORDER BY f.createdAt DESC")
    List<Friendship> findAllFriendsByUserId(@Param("userId") Long userId);
    
    // Check if friendship exists between two users
    @Query("SELECT f FROM Friendship f " +
           "WHERE (f.user1.id = :userId1 AND f.user2.id = :userId2) " +
           "OR (f.user1.id = :userId2 AND f.user2.id = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    // Delete friendship between two users (both directions)
    @Query("DELETE FROM Friendship f " +
           "WHERE (f.user1.id = :userId1 AND f.user2.id = :userId2) " +
           "OR (f.user1.id = :userId2 AND f.user2.id = :userId1)")
    void deleteFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
}
