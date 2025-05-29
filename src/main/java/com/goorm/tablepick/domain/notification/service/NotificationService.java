package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.NotificationQueue;
import java.util.List;

public interface NotificationService {
    NotificationResponse scheduleNotification(NotificationRequest request);

    void processNotificationQueue();

    void processNotificationWithNewTransaction(NotificationQueue notification);

    void handleNotificationErrorWithNewTransaction(NotificationQueue notification, Exception e);

    NotificationResponse getNotificationStatus(Long id);

    List<NotificationResponse> getMemberNotifications(Long memberId, String status);
}
