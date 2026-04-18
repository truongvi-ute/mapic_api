package com.mapic.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMomentRequest {
    private String caption;
    private Double latitude;
    private Double longitude;
    private String addressName;
    private Integer provinceId;
    private Integer districtId;
    private Integer communeId;
    private String category;
    private Boolean isPublic;
}
