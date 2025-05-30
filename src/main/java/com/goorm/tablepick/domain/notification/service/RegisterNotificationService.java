package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.member.entity.Member;

public interface RegisterNotificationService {
    void sendWelcomeNotification(Member member);
}
