package com.goorm.tablepick.domain.notification.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private Long memberId;
    private Long reservationId;
    private String type;
    private String title;
    private String body;
}
