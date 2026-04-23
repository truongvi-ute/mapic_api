package com.mapic.backend.dto.response;

import com.mapic.backend.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MomentResponse {
    private Long id;
    private String content;
    private AuthorInfo author;
    private LocationInfo location;
    private ProvinceInfo province;
    private DistrictInfo district;
    private CommuneInfo commune;
    private String category;
    private Boolean isPublic;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MediaInfo> media;
    private Long reactionCount;
    private Boolean userReacted;
    private ReactionType userReactionType; // Thêm field này
    private Long commentCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String username;
        private String fullName;
        private String avatarUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo {
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
    public static class ProvinceInfo {
        private Integer id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DistrictInfo {
        private Integer id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommuneInfo {
        private Integer id;
        private String name;
        private String code;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaInfo {
        private Long id;
        private String mediaUrl;
        private String mediaType;
        private Integer sortOrder;
    }

    public static MomentResponse fromEntity(Moment moment) {
        return fromEntityWithReactionAndComment(moment, 0L, false, null, 0L);
    }

    public static MomentResponse fromEntityWithReaction(Moment moment, long reactionCount, boolean userReacted) {
        return fromEntityWithReactionAndComment(moment, reactionCount, userReacted, null, 0L);
    }
    
    public static MomentResponse fromEntityWithReaction(Moment moment, long reactionCount, boolean userReacted, ReactionType userReactionType) {
        return fromEntityWithReactionAndComment(moment, reactionCount, userReacted, userReactionType, 0L);
    }

    public static MomentResponse fromEntityWithReactionAndComment(Moment moment, long reactionCount, boolean userReacted, long commentCount) {
        return fromEntityWithReactionAndComment(moment, reactionCount, userReacted, null, commentCount);
    }

    public static MomentResponse fromEntityWithReactionAndComment(Moment moment, long reactionCount, boolean userReacted, ReactionType userReactionType, long commentCount) {
        return MomentResponse.builder()
                .id(moment.getId())
                .content(moment.getContent())
                .author(AuthorInfo.builder()
                        .id(moment.getAuthor().getId())
                        .username(moment.getAuthor().getUsername())
                        .fullName(moment.getAuthor().getName())
                        .avatarUrl(moment.getAuthor().getUserProfile() != null 
                                ? moment.getAuthor().getUserProfile().getAvatarUrl() 
                                : null)
                        .build())
                .location(moment.getLocation() != null ? LocationInfo.builder()
                        .id(moment.getLocation().getId())
                        .latitude(moment.getLocation().getLatitude())
                        .longitude(moment.getLocation().getLongitude())
                        .address(moment.getLocation().getAddress())
                        .name(moment.getLocation().getName())
                        .build() : null)
                .province(moment.getProvince() != null ? ProvinceInfo.builder()
                        .id(moment.getProvince().getId())
                        .name(moment.getProvince().getName())
                        .code(moment.getProvince().getCode())
                        .build() : null)
                .district(moment.getDistrict() != null ? DistrictInfo.builder()
                        .id(moment.getDistrict().getId())
                        .name(moment.getDistrict().getName())
                        .code(moment.getDistrict().getCode())
                        .build() : null)
                .commune(moment.getCommune() != null ? CommuneInfo.builder()
                        .id(moment.getCommune().getId())
                        .name(moment.getCommune().getName())
                        .code(moment.getCommune().getCode())
                        .build() : null)
                .category(moment.getCategory())
                .isPublic(moment.getIsPublic())
                .status(moment.getStatus())
                .createdAt(moment.getCreatedAt())
                .updatedAt(moment.getUpdatedAt())
                .media(moment.getMedia() != null ? moment.getMedia().stream()
                        .map(m -> MediaInfo.builder()
                                .id(m.getId())
                                .mediaUrl(m.getMediaUrl())
                                .mediaType(m.getMediaType().name())
                                .sortOrder(m.getSortOrder())
                                .build())
                        .collect(Collectors.toList()) : null)
                .reactionCount(reactionCount)
                .userReacted(userReacted)
                .userReactionType(userReactionType)
                .commentCount(commentCount)
                .build();
    }
}
