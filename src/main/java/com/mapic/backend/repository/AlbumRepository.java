package com.mapic.backend.repository;

import com.mapic.backend.entity.Album;
import com.mapic.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    List<Album> findByAuthorOrderByCreatedAtDesc(User author);
    
    Optional<Album> findByIdAndAuthor(Long id, User author);
    
    boolean existsByTitleAndAuthor(String title, User author);
}
