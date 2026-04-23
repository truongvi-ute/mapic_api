package com.mapic.backend.repository;

import com.mapic.backend.entity.SOSAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SOSAuditLogRepository extends JpaRepository<SOSAuditLog, Long> {

    List<SOSAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SOSAuditLog> findByAlertIdOrderByCreatedAtDesc(Long alertId);

    List<SOSAuditLog> findByUserIdAndActionOrderByCreatedAtDesc(Long userId, String action);

    List<SOSAuditLog> findByAlertIdAndActionOrderByCreatedAtDesc(Long alertId, String action);
}
