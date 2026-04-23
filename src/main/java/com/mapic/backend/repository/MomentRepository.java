package com.mapic.backend.repository;

import com.mapic.backend.entity.Moment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {
    
    // Count moments created after a specific date
    long countByCreatedAtAfter(LocalDateTime dateTime);
    
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
    
    @Query("SELECT DISTINCT m FROM Moment m " +
           "LEFT JOIN FETCH m.author a " +
           "LEFT JOIN FETCH a.userProfile " +
           "LEFT JOIN FETCH m.location " +
           "LEFT JOIN FETCH m.province " +
           "LEFT JOIN FETCH m.district " +
           "LEFT JOIN FETCH m.commune " +
           "LEFT JOIN FETCH m.media " +
           "WHERE m.author.id = :authorId " +
           "AND m.isPublic = true " +
           "AND m.status = 'ACTIVE' " +
           "ORDER BY m.createdAt DESC")
    List<Moment> findPublicMomentsByAuthorId(@Param("authorId") Long authorId);
    
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
    
    // Explore moments with filters
    @Query("SELECT m FROM Moment m " +
           "WHERE m.status = 'ACTIVE' " +
           "AND m.isPublic = true " +
           "AND (:provinceId IS NULL OR m.province.id = :provinceId) " +
           "AND (:category IS NULL OR m.category = :category) " +
           "ORDER BY m.createdAt DESC")
    Page<Moment> exploreMoments(
            @Param("provinceId") Integer provinceId,
            @Param("category") String category,
            Pageable pageable
    );

    // Popular moments: sorted by (reactions + comments)
    @Query("SELECT m FROM Moment m " +
           "LEFT JOIN Reaction r ON r.moment = m " +
           "LEFT JOIN Comment c ON c.moment = m " +
           "WHERE m.status = 'ACTIVE' " +
           "AND m.isPublic = true " +
           "AND (:provinceId IS NULL OR m.province.id = :provinceId) " +
           "AND (:category IS NULL OR m.category = :category) " +
           "GROUP BY m.id " +
           "ORDER BY (COUNT(DISTINCT r) + COUNT(DISTINCT c)) DESC, m.createdAt DESC")
    Page<Moment> exploreMomentsPopular(
            @Param("provinceId") Integer provinceId,
            @Param("category") String category,
            Pageable pageable
    );
}
