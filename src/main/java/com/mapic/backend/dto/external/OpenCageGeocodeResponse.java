package com.mapic.backend.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenCageGeocodeResponse {
    private List<Result> results;
    private Status status;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private Components components;
        private String formatted;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Components {
        private String state;        // Province
        private String city;         // Fallback for Province in cities like HN, HCM
        @JsonProperty("city_district")
        private String cityDistrict; // District in cities
        private String county;        // District in rural areas
        private String district;      // District fallback
        private String suburb;        // Ward
        private String village;       // Commune
        private String town;          // Town (Ward level)
        private String quarter;       // Urban sub-ward
        @JsonProperty("_normalized_city")
        private String normalizedCity;
    }

    @Data
    public static class Status {
        private int code;
        private String message;
    }
}
