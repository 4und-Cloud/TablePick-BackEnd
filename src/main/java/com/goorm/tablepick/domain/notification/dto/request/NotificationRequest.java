package com.goorm.tablepick.domain.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private Long memberId;
    private Long notificationTypeId;
    private Long reservationId;
    private LocalDateTime scheduledAt;
}
