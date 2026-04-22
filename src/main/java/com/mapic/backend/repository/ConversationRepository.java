package com.mapic.backend.repository;

import com.mapic.backend.entity.Conversation;
import com.mapic.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("""
        SELECT DISTINCT c FROM Conversation c
        JOIN Participant p ON p.conversation = c
        LEFT JOIN Message lm ON lm = c.lastMessage
        WHERE p.user = :user
        ORDER BY COALESCE(lm.createdAt, c.createdAt) DESC
    """)
    List<Conversation> findAllByUser(@Param("user") User user);

    @Query("""
        SELECT c FROM Conversation c
        WHERE c.isGroup = false
        AND EXISTS (SELECT p FROM Participant p WHERE p.conversation = c AND p.user = :user1)
        AND EXISTS (SELECT p FROM Participant p WHERE p.conversation = c AND p.user = :user2)
    """)
    Optional<Conversation> findDirectConversation(@Param("user1") User user1, @Param("user2") User user2);
}
