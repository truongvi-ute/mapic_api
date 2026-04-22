package com.mapic.backend.repository;

import com.mapic.backend.entity.Message;
import com.mapic.backend.entity.MessageReaction;
import com.mapic.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    List<MessageReaction> findByMessage(Message message);

    Optional<MessageReaction> findByMessageAndUser(Message message, User user);

    void deleteByMessageAndUser(Message message, User user);

    void deleteByMessage(Message message);
}
