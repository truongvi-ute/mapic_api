package com.mapic.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendFriendRequestDto {
    @NotNull(message = "Receiver ID is required")
    private Long receiverId;
}
