package com.mapic.backend.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class DistrictDTO {
    private String name;
    private Integer code;
    @JsonProperty("division_type")
    private String divisionType;
    private String codename;
    @JsonProperty("province_code")
    private Integer provinceCode;
    private List<CommuneDTO> wards;
}
