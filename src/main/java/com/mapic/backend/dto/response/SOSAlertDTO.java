package com.mapic.backend.dto.response;

import com.mapic.backend.entity.LocationStatus;
import com.mapic.backend.entity.SOSAlertStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SOSAlertDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private LocalDateTime triggeredAt;
    private LocalDateTime resolvedAt;
    private SOSAlertStatus status;
    private Double latitude;
    private Double longitude;
    private String message;
    private LocationStatus locationStatus;
    private Integer recipientCount;
}
