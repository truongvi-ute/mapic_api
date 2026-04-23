package com.mapic.backend.repository;

import com.mapic.backend.entity.SOSAlertRecipient;
import com.mapic.backend.entity.SOSAlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SOSAlertRecipientRepository extends JpaRepository<SOSAlertRecipient, Long> {

    List<SOSAlertRecipient> findByAlertId(Long alertId);

    @Query("SELECT r FROM SOSAlertRecipient r " +
           "WHERE r.recipient.id = :recipientId " +
           "AND r.alert.status = :status")
    List<SOSAlertRecipient> findByRecipientIdAndAlert_Status(
            @Param("recipientId") Long recipientId,
            @Param("status") SOSAlertStatus status);
    
    @Query("SELECT r FROM SOSAlertRecipient r " +
           "WHERE r.alert.id = :alertId " +
           "AND r.recipient.id = :recipientId")
    Optional<SOSAlertRecipient> findByAlertIdAndRecipientId(
            @Param("alertId") Long alertId,
            @Param("recipientId") Long recipientId);
}
