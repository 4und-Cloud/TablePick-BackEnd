package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.notification.constant.NotificationStatus;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.entity.NotificationQueue;
import com.goorm.tablepick.domain.notification.entity.NotificationType;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private NotificationQueueRepository notificationQueueRepository;

    @Mock
    private NotificationTypeRepository notificationTypeRepository;

    @Mock
    private FCMService fcmService;

    @Mock
    private FCMTokenService fcmTokenService;

    @Test
    @DisplayName("알림 예약 성공")
    void scheduleNotificationSuccess() {
        // given
        NotificationRequest request = createNotificationRequest();
        NotificationType type = createNotificationType();
        NotificationQueue queue = createNotificationQueue(type);

        given(notificationTypeRepository.findById(any())).willReturn(Optional.of(type));
        given(notificationQueueRepository.save(any())).willReturn(queue);

        // when
        var response = notificationService.scheduleNotification(request);

        // then
        assertThat(response.getId()).isEqualTo(queue.getId());
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.PENDING.name());
        verify(notificationQueueRepository).save(any());
    }

    @Test
    @DisplayName("예약된 알림 처리")
    void processNotificationQueueSuccess() {
        // given
        NotificationType type = createNotificationType();
        NotificationQueue queue = createNotificationQueue(type);
        given(notificationQueueRepository.findByStatusAndScheduledAtBefore(any(), any()))
                .willReturn(Arrays.asList(queue));
        given(fcmTokenService.getFcmToken(any())).willReturn("test-token");

        // when
        notificationService.processNotificationQueue();

        // then
        verify(fcmService).sendMessage(any(), any(), any(), any());
    }

    private NotificationRequest createNotificationRequest() {
        return NotificationRequest.builder()
                .memberId(1L)
                .notificationTypeId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();
    }

    private NotificationType createNotificationType() {
        return NotificationType.builder()
                .id(1L)
                .type("TEST")
                .title("Test Title")
                .body("Test Body")
                .url("test-url")
                .build();
    }

    private NotificationQueue createNotificationQueue(NotificationType type) {
        return NotificationQueue.builder()
                .id(1L)
                .notificationType(type)
                .memberId(1L)
                .reservationId(1L)
                .status(NotificationStatus.PENDING.name())
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
