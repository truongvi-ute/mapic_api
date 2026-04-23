package com.mapic.backend.service;

import com.mapic.backend.dto.request.TriggerSOSRequest;
import com.mapic.backend.dto.response.SOSAlertDTO;
import com.mapic.backend.dto.response.TriggerSOSResponse;
import java.util.List;

public interface SOSService {
    
    TriggerSOSResponse createSOSAlert(Long userId, TriggerSOSRequest request);
    
    void resolveSOSAlert(Long alertId, Long userId);
    
    ActiveAlertsResponse getActiveAlerts(Long userId);
    
    AlertHistoryResponse getAlertHistory(Long userId, Integer limit, Integer offset);
    
    boolean validateAlertOwnership(Long alertId, Long userId);
    
    void markAlertAsViewed(Long alertId, Long userId);
    
    record ActiveAlertsResponse(
        SOSAlertDTO asSender,
        List<SOSAlertDTO> asRecipient
    ) {}
    
    record AlertHistoryResponse(
        List<SOSAlertDTO> alerts,
        Integer total,
        Integer limit,
        Integer offset
    ) {}
}
