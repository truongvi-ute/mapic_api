package com.mapic.backend.repository;

import com.mapic.backend.entity.Moment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {
    
    @Query("SELECT DISTINCT m FROM Moment m " +
           "LEFT JOIN FETCH m.author a " +
           "LEFT JOIN FETCH a.userProfile " +
           "LEFT JOIN FETCH m.location " +
           "LEFT JOIN FETCH m.province " +
           "LEFT JOIN FETCH m.district " +
           "LEFT JOIN FETCH m.commune " +
           "LEFT JOIN FETCH m.media " +
           "WHERE m.author.id = :authorId " +
           "ORDER BY m.createdAt DESC")
    List<Moment> findByAuthorId(@Param("authorId") Long authorId);
    
    // Query for feed: public moments + private moments from friends
    @Query("SELECT m FROM Moment m " +
           "WHERE m.status = 'ACTIVE' " +
           "AND (m.isPublic = true " +
           "     OR (m.isPublic = false AND EXISTS (" +
           "         SELECT f FROM Friendship f " +
           "         WHERE ((f.user1.id = :userId AND f.user2.id = m.author.id) " +
           "              OR (f.user2.id = :userId AND f.user1.id = m.author.id))" +
           "     ))" +
           ") " +
           "ORDER BY m.createdAt DESC")
    Page<Moment> findFeedMoments(@Param("userId") Long userId, Pageable pageable);
    
    // Count query for pagination
    @Query("SELECT COUNT(m) FROM Moment m " +
           "WHERE m.status = 'ACTIVE' " +
           "AND (m.isPublic = true " +
           "     OR (m.isPublic = false AND EXISTS (" +
           "         SELECT f FROM Friendship f " +
           "         WHERE ((f.user1.id = :userId AND f.user2.id = m.author.id) " +
           "              OR (f.user2.id = :userId AND f.user1.id = m.author.id))" +
           "     ))" +
           ")")
    long countFeedMoments(@Param("userId") Long userId);
}
