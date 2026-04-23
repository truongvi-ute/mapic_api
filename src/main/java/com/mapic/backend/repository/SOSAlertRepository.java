package com.mapic.backend.repository;

import com.mapic.backend.entity.SOSAlert;
import com.mapic.backend.entity.SOSAlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SOSAlertRepository extends JpaRepository<SOSAlert, Long> {

    Optional<SOSAlert> findBySenderIdAndStatus(Long senderId, SOSAlertStatus status);

    List<SOSAlert> findBySenderIdOrderByTriggeredAtDesc(Long senderId);

    @Query("SELECT DISTINCT a FROM SOSAlert a " +
           "JOIN SOSAlertRecipient r ON r.alert.id = a.id " +
           "WHERE r.recipient.id = :userId " +
           "AND a.status = com.mapic.backend.entity.SOSAlertStatus.ACTIVE " +
           "ORDER BY a.triggeredAt DESC")
    List<SOSAlert> findActiveAlertsByRecipientId(@Param("userId") Long userId);
}
