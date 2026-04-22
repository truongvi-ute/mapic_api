package com.mapic.backend.repository;

import com.mapic.backend.entity.Comment;
import com.mapic.backend.entity.Moment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Get top level comments for a moment
    Page<Comment> findByMomentAndParentCommentIsNullOrderByCreatedAtDesc(Moment moment, Pageable pageable);
    
    // Get all comments for a moment (if needed)
    List<Comment> findByMomentOrderByCreatedAtAsc(Moment moment);
    
    // Get replies for a specific comment
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.moment = :moment")
    long countByMoment(@Param("moment") Moment moment);
}
