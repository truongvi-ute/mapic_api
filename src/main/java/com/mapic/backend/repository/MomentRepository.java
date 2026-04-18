package com.mapic.backend.repository;

import com.mapic.backend.entity.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {
    List<Moment> findByAuthorId(Long authorId);
}
