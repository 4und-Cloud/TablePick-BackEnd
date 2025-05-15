package com.goorm.tablepick.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "알림 응답")
public class NotificationResponse {
    @Schema(description = "알림 ID", example = "1")
    private Long id;

    @Schema(description = "알림 상태", example = "PENDING", allowableValues = {"PENDING", "SENT", "FAILED"})
    private String status;

    @Schema(description = "예약 시간", example = "2025-05-14T19:30:00")
    private LocalDateTime scheduledAt;

    @Schema(description = "발송 시간", example = "2025-05-14T19:30:05")
    private LocalDateTime sentAt;

    @Schema(description = "회원 ID", example = "1")
    private Long memberId;

    @Schema(description = "예약 ID", example = "100")
    private Long reservationId;

    @Schema(description = "알림 타입", example = "RESERVATION_REMINDER")
    private String type;

    @Schema(description = "알림 제목", example = "예약 알림")
    private String title;

    @Schema(description = "알림 내용", example = "30분 후에 예약이 있습니다.")
    private String body;
}
