package com.goorm.tablepick.domain.notification.service;

public interface FCMTokenService {
    void updateFcmToken(Long memberId, String fcmToken);

    void updateFcmTokenToNull(Long memberId);

    String getFcmToken(Long memberId);
}
