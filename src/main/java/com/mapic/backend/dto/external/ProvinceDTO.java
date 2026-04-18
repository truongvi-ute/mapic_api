package com.mapic.backend.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ProvinceDTO {
    private String name;
    private Integer code;
    @JsonProperty("division_type")
    private String divisionType;
    private String codename;
    @JsonProperty("phone_code")
    private Integer phoneCode;
    private List<DistrictDTO> districts;
}
