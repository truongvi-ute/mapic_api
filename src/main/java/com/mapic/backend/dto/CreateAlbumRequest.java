package com.mapic.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlbumRequest {
    @NotBlank(message = "Tên album không được để trống")
    @Size(max = 100, message = "Tên album không được vượt quá 100 ký tự")
    private String title;
    
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;
}
