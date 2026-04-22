package com.mapic.backend.repository;

import com.mapic.backend.entity.Album;
import com.mapic.backend.entity.AlbumItem;
import com.mapic.backend.entity.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumItemRepository extends JpaRepository<AlbumItem, Long> {
    List<AlbumItem> findByAlbumOrderBySortOrderAsc(Album album);

    List<AlbumItem> findByAlbumOrderByAddedAtDesc(Album album);
    
    Optional<AlbumItem> findByAlbumAndMoment(Album album, Moment moment);
    
    boolean existsByAlbumAndMoment(Album album, Moment moment);
    
    long countByAlbum(Album album);
    
    void deleteByAlbum(Album album);

    @Query("SELECT COALESCE(MAX(a.sortOrder), -1) FROM AlbumItem a WHERE a.album = :album")
    Integer findMaxSortOrderByAlbum(@Param("album") Album album);
}
