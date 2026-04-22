package com.mapic.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateUserStatusRequest {
    
    @NotBlank(message = "Status is required")
    private String status; // ACTIVE, WARNING, SUSPENDED, BANNED
    
    private String reason;
    private String note;
    private LocalDateTime expiresAt;
    private boolean notifyUser = true;
}
