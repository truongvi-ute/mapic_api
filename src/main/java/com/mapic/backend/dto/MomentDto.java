package com.mapic.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomentDto {
    private Long id;
    private String content;
    private AuthorDto author;
    private LocationDto location;
    private ProvinceDto province;
    private DistrictDto district;
    private CommuneDto commune;
    private String category;
    private Boolean isPublic;
    private String status;
    private LocalDateTime createdAt;
    private List<MediaDto> media;
    private Long reactionCount;
    private Boolean userReacted;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorDto {
        private Long id;
        private String username;
        private String fullName;
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationDto {
        private Long id;
        private Double latitude;
        private Double longitude;
        private String address;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProvinceDto {
        private Long id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistrictDto {
        private Long id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommuneDto {
        private Long id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaDto {
        private Long id;
        private String mediaUrl;
        private String mediaType;
        private Integer sortOrder;
    }
}
