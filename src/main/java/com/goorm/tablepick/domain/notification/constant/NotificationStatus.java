package com.goorm.tablepick.domain.notification.constant;

public enum NotificationStatus {

    // 대기 중인 알림
    PENDING,

    // 전송 완료된 알림
    SENT,

    // 전송 실패한 알림
    FAILED,

    // 취소된 알림
    CANCELLED
}
