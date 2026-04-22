package com.mapic.backend.repository;

import com.mapic.backend.entity.Conversation;
import com.mapic.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        LEFT JOIN FETCH m.sender s
        LEFT JOIN FETCH s.userProfile
        WHERE m.conversation = :conversation
        ORDER BY m.createdAt DESC
    """)
    Page<Message> findByConversationOrderByCreatedAtDesc(
            @Param("conversation") Conversation conversation, Pageable pageable);

    List<Message> findAllByConversation(Conversation conversation);
}
