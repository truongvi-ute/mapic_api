package com.mapic.backend.dto;

import com.mapic.backend.entity.LocationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdatePayload {
    private Long alertId;
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
    private LocationStatus locationStatus;
}
