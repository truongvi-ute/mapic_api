package com.mapic.backend.repository;

import com.mapic.backend.entity.Reaction;
import com.mapic.backend.entity.Comment;
import com.mapic.backend.entity.Moment;
import com.mapic.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    
    Optional<Reaction> findByUserAndMoment(User user, Moment moment);
    void deleteByUserAndMoment(User user, Moment moment);
    long countByMoment(Moment moment);
    boolean existsByUserAndMoment(User user, Moment moment);

    Optional<Reaction> findByUserAndComment(User user, Comment comment);
    void deleteByUserAndComment(User user, Comment comment);
    void deleteByComment(Comment comment);
    long countByComment(Comment comment);
    boolean existsByUserAndComment(User user, Comment comment);
}
