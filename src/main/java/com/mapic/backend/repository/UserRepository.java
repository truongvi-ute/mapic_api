package com.mapic.backend.repository;

import com.mapic.backend.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    // Spring Data JPA sẽ tự hiểu: WHERE username = ? OR email = ?
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Search users by name or username
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.userProfile " +
           "WHERE LOWER(u.name) LIKE :query " +
           "OR LOWER(u.username) LIKE :query " +
           "ORDER BY u.name ASC")
    List<User> searchByNameOrUsername(@Param("query") String query);
    
    // Search users by username or full name with pagination
    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.userProfile " +
           "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "ORDER BY u.name ASC")
    List<User> searchByUsernameOrFullName(@Param("search") String search, Pageable pageable);
}
