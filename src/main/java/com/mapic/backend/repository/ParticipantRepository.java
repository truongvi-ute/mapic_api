package com.mapic.backend.repository;

import com.mapic.backend.entity.Conversation;
import com.mapic.backend.entity.Participant;
import com.mapic.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

    List<Participant> findByConversation(Conversation conversation);

    Optional<Participant> findByConversationAndUser(Conversation conversation, User user);

    boolean existsByConversationAndUser(Conversation conversation, User user);

    void deleteByConversationAndUser(Conversation conversation, User user);

    // Query bằng ID để tránh entity comparison issues
    @Query("SELECT COUNT(p) > 0 FROM Participant p WHERE p.conversation.id = :convId AND p.user.id = :userId")
    boolean existsByConversationIdAndUserId(@Param("convId") Long convId, @Param("userId") Long userId);
}
