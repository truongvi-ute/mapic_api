package com.mapic.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ModerationActionRequest {
    
    @NotBlank(message = "Action is required")
    private String action; // IGNORE, HIDE, DELETE, APPROVE
    
    private String reason;
    private String note;
    private boolean notifyUser = true;
    private boolean notifyReporter = true;
}
