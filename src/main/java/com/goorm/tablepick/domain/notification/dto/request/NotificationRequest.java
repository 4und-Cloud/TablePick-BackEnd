package com.goorm.tablepick.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "알림 예약 요청")
public class NotificationRequest {
    @Schema(description = "회원 ID", example = "1", required = true)
    private Long memberId;

    @Schema(description = "알림 타입 ID", example = "1", required = true)
    private Long notificationTypeId;

    @Schema(description = "예약 ID", example = "100", required = true)
    private Long reservationId;

    @Schema(description = "예약 시간 (ISO-8601 형식)", example = "2025-05-14T19:30:00", required = true)
    private LocalDateTime scheduledAt;
}
