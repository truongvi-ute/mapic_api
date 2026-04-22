package com.mapic.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlbumDto {
    private Long id;
    private String title;
    private String description;
    private String coverImageUrl;
    private Long itemCount;
    private LocalDateTime createdAt;
    private List<MomentDto> moments;
}
