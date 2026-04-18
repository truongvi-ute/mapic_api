package com.mapic.backend.dto;

import com.mapic.backend.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    
    private String bio;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String location;
}
