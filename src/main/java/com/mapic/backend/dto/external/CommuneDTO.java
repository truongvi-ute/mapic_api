package com.mapic.backend.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommuneDTO {
    private String name;
    private Integer code;
    @JsonProperty("division_type")
    private String divisionType;
    private String codename;
    @JsonProperty("district_code")
    private Integer districtCode;
}
