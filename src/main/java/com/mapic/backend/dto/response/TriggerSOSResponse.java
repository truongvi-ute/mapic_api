package com.mapic.backend.dto.response;

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
public class TriggerSOSResponse {
    private Long alertId;
    private LocalDateTime triggeredAt;
    private Integer recipientCount;
    private List<RecipientInfo> recipients;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecipientInfo {
        private Long userId;
        private String name;
        private String avatarUrl;
    }
}
