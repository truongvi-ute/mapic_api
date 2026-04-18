package com.mapic.backend.repository;

import com.mapic.backend.entity.MomentMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MomentMediaRepository extends JpaRepository<MomentMedia, Long> {
    List<MomentMedia> findByMomentId(Long momentId);
}
