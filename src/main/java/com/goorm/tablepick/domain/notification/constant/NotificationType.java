package com.goorm.tablepick.domain.notification.constant;

// 알림 유형을 정의하는 상수 클래스
public class NotificationType {

    // 회원가입 환영 알림
    public static final String WELCOME = "WELCOME";

    // 예약 완료 알림
    public static final String RESERVATION_CONFIRMATION = "RESERVATION_CONFIRMATION";

    // 예약 1일 전 알림
    public static final String ONE_DAY_BEFORE = "ONE_DAY_BEFORE";

    // 예약 3시간 전 알림
    public static final String THREE_HOURS_BEFORE = "THREE_HOURS_BEFORE";

    // 예약 1시간 전 알림
    public static final String ONE_HOUR_BEFORE = "ONE_HOUR_BEFORE";

    // 예약 3시간 후 알림, 리뷰 요청 알림
    public static final String REVIEW_REQUEST = "REVIEW_REQUEST";

    // 유틸리티 클래스의 인스턴스화 방지를 위한 private 생성자
    private NotificationType() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }
}
