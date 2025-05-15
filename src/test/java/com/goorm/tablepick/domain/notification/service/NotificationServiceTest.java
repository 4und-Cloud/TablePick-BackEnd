package com.goorm.tablepick.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.goorm.tablepick.domain.notification.constant.NotificationStatus;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.NotificationLog;
import com.goorm.tablepick.domain.notification.entity.NotificationQueue;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationLogRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.global.exception.NotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationQueueRepository notificationQueueRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private NotificationTypesRepository notificationTypesRepository;

    @Mock
    private FCMService fcmService;

    @Mock
    private FCMTokenService fcmTokenService;

    @InjectMocks
    private NotificationService notificationService;

    private NotificationTypes notificationType;
    private NotificationQueue notificationQueue;
    private NotificationRequest notificationRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        notificationType = mock(NotificationTypes.class);
        when(notificationType.getId()).thenReturn(1L);
        when(notificationType.getType()).thenReturn("RESERVATION_1DAY_BEFORE");
        when(notificationType.getTitle()).thenReturn("예약 1일 전 알림");
        when(notificationType.getBody()).thenReturn("내일은 {restaurantName} 예약이 있습니다.");
        when(notificationType.getUrl()).thenReturn("/reservations/{id}");

        notificationQueue = mock(NotificationQueue.class);
        when(notificationQueue.getId()).thenReturn(1L);
        when(notificationQueue.getMemberId()).thenReturn(1L);
        when(notificationQueue.getReservationId()).thenReturn(1L);
        when(notificationQueue.getNotificationTypes()).thenReturn(notificationType);
        when(notificationQueue.getStatus()).thenReturn(NotificationStatus.PENDING.name());
        when(notificationQueue.getScheduledAt()).thenReturn(now.plusHours(1));
        when(notificationQueue.getRetryCount()).thenReturn(0);

        notificationRequest = NotificationRequest.builder()
                .memberId(1L)
                .reservationId(1L)
                .notificationTypeId(1L)
                .scheduledAt(now.plusHours(1))
                .build();
    }

    @Test
    @DisplayName("알림 예약 테스트")
    void scheduleNotification_ShouldCreateNotificationQueue() {
        // Given
        when(notificationTypesRepository.findById(anyLong())).thenReturn(Optional.of(notificationType));
        when(notificationQueueRepository.save(any(NotificationQueue.class))).thenReturn(notificationQueue);

        // When
        NotificationResponse response = notificationService.scheduleNotification(notificationRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(NotificationStatus.PENDING.name(), response.getStatus());

        verify(notificationTypesRepository, times(1)).findById(1L);
        verify(notificationQueueRepository, times(1)).save(any(NotificationQueue.class));
    }

    @Test
    @DisplayName("존재하지 않는 알림 타입으로 알림 예약 테스트")
    void scheduleNotification_WithNonExistingType_ShouldThrowException() {
        // Given
        when(notificationTypesRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            notificationService.scheduleNotification(notificationRequest);
        });

        assertEquals("Notification type not found", exception.getMessage());
        assertEquals("TYPE_NOT_FOUND", exception.getErrorCode());
        verify(notificationTypesRepository, times(1)).findById(1L);
        verify(notificationQueueRepository, never()).save(any(NotificationQueue.class));
    }

    @Test
    @DisplayName("알림 큐 처리 테스트")
    void processNotificationQueue_ShouldProcessPendingNotifications() {
        // Given
        List<NotificationQueue> pendingNotifications = Collections.singletonList(notificationQueue);
        when(notificationQueueRepository.findByStatusAndScheduledAtBefore(
                eq(NotificationStatus.PENDING.name()), any(LocalDateTime.class)))
                .thenReturn(pendingNotifications);

        when(fcmTokenService.getFcmToken(anyLong())).thenReturn("test-fcm-token");
        when(fcmService.sendMessage(anyString(), anyString(), anyString(), anyMap())).thenReturn("message-id");

        // When
        notificationService.processNotificationQueue();

        // Then
        verify(notificationQueueRepository, times(1))
                .findByStatusAndScheduledAtBefore(eq(NotificationStatus.PENDING.name()), any(LocalDateTime.class));
        verify(fcmTokenService, times(1)).getFcmToken(1L);
        verify(fcmService, times(1)).sendMessage(eq("test-fcm-token"), anyString(), anyString(), anyMap());
        verify(notificationQueue, times(1)).setStatus(NotificationStatus.SENT.name());
        verify(notificationQueueRepository, times(1)).save(notificationQueue);
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }

    @Test
    @DisplayName("알림 상태 조회 테스트")
    void getNotificationStatus_ShouldReturnNotification() {
        // Given
        when(notificationQueueRepository.findById(anyLong())).thenReturn(Optional.of(notificationQueue));

        // When
        NotificationResponse response = notificationService.getNotificationStatus(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(NotificationStatus.PENDING.name(), response.getStatus());

        verify(notificationQueueRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 알림 상태 조회 테스트")
    void getNotificationStatus_WithNonExistingId_ShouldThrowException() {
        // Given
        when(notificationQueueRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            notificationService.getNotificationStatus(999L);
        });

        assertEquals("Notification not found", exception.getMessage());
        assertEquals("NOTIFICATION_NOT_FOUND", exception.getErrorCode());
        verify(notificationQueueRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("회원 알림 목록 조회 테스트 - 상태 필터 있음")
    void getMemberNotifications_WithStatusFilter_ShouldReturnFilteredList() {
        // Given
        List<NotificationQueue> notifications = Collections.singletonList(notificationQueue);
        when(notificationQueueRepository.findByMemberIdAndStatus(anyLong(), anyString())).thenReturn(notifications);

        // When
        List<NotificationResponse> responses = notificationService.getMemberNotifications(1L, "PENDING");

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(NotificationStatus.PENDING.name(), responses.get(0).getStatus());

        verify(notificationQueueRepository, times(1)).findByMemberIdAndStatus(1L, "PENDING");
        verify(notificationQueueRepository, never()).findByMemberId(anyLong());
    }

    @Test
    @DisplayName("회원 알림 목록 조회 테스트 - 상태 필터 없음")
    void getMemberNotifications_WithoutStatusFilter_ShouldReturnAllList() {
        // Given
        List<NotificationQueue> notifications = Collections.singletonList(notificationQueue);
        when(notificationQueueRepository.findByMemberId(anyLong())).thenReturn(notifications);

        // When
        List<NotificationResponse> responses = notificationService.getMemberNotifications(1L, null);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(NotificationStatus.PENDING.name(), responses.get(0).getStatus());

        verify(notificationQueueRepository, never()).findByMemberIdAndStatus(anyLong(), anyString());
        verify(notificationQueueRepository, times(1)).findByMemberId(1L);
    }
}
