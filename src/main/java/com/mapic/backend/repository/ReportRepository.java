package com.mapic.backend.repository;

import com.mapic.backend.entity.Report;
import com.mapic.backend.entity.ReportStatus;
import com.mapic.backend.entity.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    // Tìm reports theo status
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    
    // Tìm reports theo target type
    Page<Report> findByTargetType(ReportTargetType targetType, Pageable pageable);
    
    // Tìm reports theo status và type
    Page<Report> findByStatusAndTargetType(
        ReportStatus status, 
        ReportTargetType targetType, 
        Pageable pageable
    );
    
    // Đếm reports theo target
    int countByTargetIdAndTargetType(Long targetId, ReportTargetType targetType);
    
    // Tìm reports của một user
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    
    // Tìm reports về một content cụ thể
    List<Report> findByTargetIdAndTargetType(Long targetId, ReportTargetType targetType);
    
    // Kiểm tra user đã report content chưa
    boolean existsByReporterIdAndTargetIdAndTargetType(
        Long reporterId, 
        Long targetId, 
        ReportTargetType targetType
    );
    
    // Statistics queries
    long countByStatus(ReportStatus status);
    
    @Query("SELECT r.targetType, COUNT(r) FROM Report r GROUP BY r.targetType")
    List<Object[]> countByTargetType();
    
    // Get all reports with filters
    @Query("SELECT r FROM Report r " +
           "WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:targetType IS NULL OR r.targetType = :targetType) " +
           "ORDER BY r.createdAt DESC")
    Page<Report> findWithFilters(
        @Param("status") ReportStatus status,
        @Param("targetType") ReportTargetType targetType,
        Pageable pageable
    );
}
