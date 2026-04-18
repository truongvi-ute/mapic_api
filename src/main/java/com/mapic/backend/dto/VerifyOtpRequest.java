package com.mapic.backend.dto;

import com.mapic.backend.entity.OtpType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpRequest {
    
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "OTP code cannot be empty")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String code;

    // Không bắt buộc @NotNull để tương thích với Frontend cũ
    private OtpType type;
}
