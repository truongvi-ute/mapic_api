package com.mapic.backend.dto.request;

import com.mapic.backend.entity.ReportTargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private Long targetId;
    private ReportTargetType targetType;
    private String reason; // "nội dung sai lệch", "vi phạm tiêu chuẩn cộng đồng", "ngôn từ thù ghét", "khác"
}
