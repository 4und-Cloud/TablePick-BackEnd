package com.goorm.tablepick.domain.notification.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTypes {
    RESERVATION_1DAY_BEFORE(
            "예약 1일 전 알림",
            "내일 이 시간에 {restaurantName} 예약이 있습니다!",
            "/reservations/{id}"
    ),

    RESERVATION_3HOURS_BEFORE(
            "예약 3시간 전 알림",
            "3시간 뒤에 {restaurantName} 예약이 있습니다! 까먹지 않게 또 알려드릴게요!",
            "/reservations/{id}"
    ),

    RESERVATION_1HOUR_BEFORE(
            "예약 1시간 전 알림",
            "예약하신 {restaurantName}이 열심히 준비하고 있습니다! 1시간 뒤에 늦지 않게 입장해주세용~!",
            "/reservations/{id}"
    );

    private final String title;
    private final String bodyTemplate;
    private final String url;

}
