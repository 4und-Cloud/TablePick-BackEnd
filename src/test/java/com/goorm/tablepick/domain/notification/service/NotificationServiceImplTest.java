package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.global.exception.NotificationException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class NotificationServiceImplTest {
    
    @MockBean
    private NotificationQueueRepository notificationQueueRepository;
    
    @MockBean
    private NotificationLogRepository notificationLogRepository;
    
    @MockBean
    private FCMService fcmService;
    
    @MockBean
    private FCMTokenService fcmTokenService;
    
    @MockBean
    private ReservationRepository reservationRepository;
    
    @MockBean
    private NotificationTypesRepository notificationTypesRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @AfterEach
    void tearDown() {
        notificationQueueRepository.deleteAllInBatch();
        notificationLogRepository.deleteAllInBatch();
        reservationRepository.deleteAllInBatch();
        notificationTypesRepository.deleteAllInBatch();
        reset(fcmService, fcmTokenService);
    }
    
    @DisplayName("유효한 요청으로 알림을 성공적으로 예약한다")
    @Test
    void scheduleNotification_success() {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .memberId(1L)
                .notificationTypeId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now().plusHours(1))
                .build();
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인")
                .body("예약이 완료되었습니다.").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .reservationId(1L)
                .scheduledAt(request.getScheduledAt())
                .status(NotificationStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();
        
        when(notificationTypesRepository.findById(1L)).thenReturn(Optional.of(type));
        when(notificationQueueRepository.save(any(NotificationQueue.class))).thenReturn(queue);
        when(notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(1L))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());
        
        // when
        NotificationResponse response = notificationService.scheduleNotification(request);
        
        // then
        assertThat(String.valueOf(response)).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.PENDING.name());
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getReservationId()).isEqualTo(1L);
        assertThat(response.getType()).isEqualTo("RESERVATION_CONFIRM");
        assertThat(response.getTitle()).isEqualTo("예약 확인");
        assertThat(response.getBody()).isEqualTo("예약이 완료되었습니다.");
        
        verify(notificationTypesRepository, times(1)).findById(1L);
        verify(notificationQueueRepository, times(1)).save(any(NotificationQueue.class));
    }
    
    @DisplayName("과거 시간으로 알림 예약 시 현재 시간으로 설정된다")
    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void scheduleNotification_pastScheduledAt(CapturedOutput capturedOutput) {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .memberId(1L)
                .notificationTypeId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now().minusHours(1))
                .build();
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();
        
        when(notificationTypesRepository.findById(1L)).thenReturn(Optional.of(type));
        when(notificationQueueRepository.save(any(NotificationQueue.class))).thenReturn(queue);
        when(notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(1L))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());
        
        // when
        NotificationResponse response = notificationService.scheduleNotification(request);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getScheduledAt()).isNotEqualTo(request.getScheduledAt());
        assertThat(capturedOutput.getOut()).contains("예약 시간이 현재 시간보다 이전이거나 null입니다. 현재 시간으로 설정합니다.");
        
        verify(notificationTypesRepository, times(1)).findById(1L);
        verify(notificationQueueRepository, times(1)).save(any(NotificationQueue.class));
    }
    
    @DisplayName("알림 타입이 존재하지 않을 때 예외를 던진다")
    @Test
    void scheduleNotification_typeNotFound() {
        // given
        NotificationRequest request = NotificationRequest.builder()
                .memberId(1L)
                .notificationTypeId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now())
                .build();
        when(notificationTypesRepository.findById(1L)).thenReturn(Optional.empty());
        
        // when & then
        NotificationException exception = assertThrows(NotificationException.class,
                () -> notificationService.scheduleNotification(request));
        assertThat(exception.getMessage()).isEqualTo("Notification type not found");
        assertThat(exception.getErrorCode()).isEqualTo("TYPE_NOT_FOUND");
        
        verify(notificationTypesRepository, times(1)).findById(1L);
        verify(notificationQueueRepository, times(0)).save(any());
    }
    
    @DisplayName("유효한 알림을 성공적으로 처리한다")
    @Test
    void processNotification_success() throws FirebaseMessagingException {
        // given
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인")
                .body("예약이 완료되었습니다.").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING.name())
                .build();
        Reservation reservation = mock(Reservation.class);
        when(reservation.getRestaurantName()).thenReturn("Test Restaurant");
        when(fcmTokenService.getFcmToken(1L)).thenReturn("valid-fcm-token");
        when(reservationRepository.getReservationById(1L)).thenReturn(reservation);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(fcmService.sendMessage(anyString(), anyString(), anyString(), any())).thenReturn("message_id_12345");
        when(notificationQueueRepository.save(any(NotificationQueue.class))).thenReturn(queue);
        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(mock(NotificationLog.class));
        
        // when
        notificationService.processNotificationWithNewTransaction(queue);
        
        // then
        assertThat(queue.getStatus()).isEqualTo(NotificationStatus.SENT.name());
        verify(fcmTokenService, times(1)).getFcmToken(1L);
        verify(fcmService, times(1)).sendMessage(anyString(), anyString(), anyString(), any());
        verify(notificationQueueRepository, times(1)).save(queue);
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }
    
    @DisplayName("FCM 토큰이 없으면 알림이 실패로 처리된다")
    @Test
    void processNotification_noFcmToken() {
        // given
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING.name())
                .build();
        when(fcmTokenService.getFcmToken(1L)).thenReturn(null);
        when(notificationQueueRepository.save(any(NotificationQueue.class))).thenReturn(queue);
        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(mock(NotificationLog.class));
        
        // when
        notificationService.processNotificationWithNewTransaction(queue);
        
        // then
        assertThat(queue.getStatus()).isEqualTo(NotificationStatus.FAILED.name());
        verify(fcmTokenService, times(1)).getFcmToken(1L);
        verify(fcmService, times(0)).sendMessage(anyString(), anyString(), anyString(), any());
        verify(notificationQueueRepository, times(1)).save(queue);
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
    }
    
    @DisplayName("FCM 메시지 전송 실패 시 재시도 처리한다")
    @Test
    void handleNotificationError_retry() throws FirebaseMessagingException {
        // given
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING.name())
                .retryCount(0)
                .build();
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessagingErrorCode()).thenReturn(MessagingErrorCode.THIRD_PARTY_AUTH_ERROR);
        when(exception.getMessage()).thenReturn("Auth error");
        when(notificationQueueRepository.save(any(NotificationQueue.class))).thenReturn(queue);
        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(mock(NotificationLog.class));
        
        // when
        notificationService.handleNotificationErrorWithNewTransaction(queue, exception);
        
        // then
        assertThat(queue.getRetryCount()).isEqualTo(1);
        assertThat(queue.getScheduledAt()).isAfter(LocalDateTime.now());
        verify(notificationQueueRepository, times(1)).save(queue);
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
        verify(fcmTokenService, times(0)).updateFcmTokenToNull(anyLong());
    }
    
    @DisplayName("최대 재시도 횟수 초과 시 알림 실패 처리")
    @Test
    void handleNotificationError_maxRetryExceeded() throws FirebaseMessagingException {
        // given
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.PENDING.name())
                .retryCount(3)
                .build();
        FirebaseMessagingException exception = mock(FirebaseMessagingException.class);
        when(exception.getMessagingErrorCode()).thenReturn(MessagingErrorCode.THIRD_PARTY_AUTH_ERROR);
        when(exception.getMessage()).thenReturn("Auth error");
        when(notificationQueueRepository.save(any(NotificationQueue.class))).thenReturn(queue);
        when(notificationLogRepository.save(any(NotificationLog.class))).thenReturn(mock(NotificationLog.class));
        
        // when
        notificationService.handleNotificationErrorWithNewTransaction(queue, exception);
        
        // then
        assertThat(queue.getStatus()).isEqualTo(NotificationStatus.FAILED.name());
        verify(notificationQueueRepository, times(1)).save(queue);
        verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
        verify(fcmTokenService, times(0)).updateFcmTokenToNull(anyLong());
    }
    
    @DisplayName("유효한 알림 ID로 알림 상태를 조회한다")
    @Test
    void getNotificationStatus_success() {
        // given
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .reservationId(1L)
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.SENT.name())
                .build();
        when(notificationQueueRepository.findById(1L)).thenReturn(Optional.of(queue));
        when(notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(1L))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());
        
        // when
        NotificationResponse response = notificationService.getNotificationStatus(1L);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT.name());
        verify(notificationQueueRepository, times(1)).findById(1L);
    }
    
    @DisplayName("알림 ID가 존재하지 않을 때 예외를 던진다")
    @Test
    void getNotificationStatus_notFound() {
        // given
        when(notificationQueueRepository.findById(1L)).thenReturn(Optional.empty());
        
        // when & then
        NotificationException exception = assertThrows(NotificationException.class,
                () -> notificationService.getNotificationStatus(1L));
        assertThat(exception.getMessage()).isEqualTo("Notification not found");
        assertThat(exception.getErrorCode()).isEqualTo("NOTIFICATION_NOT_FOUND");
        verify(notificationQueueRepository, times(1)).findById(1L);
    }
    
    @DisplayName("회원의 알림 목록을 상태 필터링 없이 조회한다")
    @Test
    void getMemberNotifications_withoutStatus() {
        // given
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.SENT.name())
                .build();
        when(notificationQueueRepository.findByMemberId(1L)).thenReturn(List.of(queue));
        when(notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(1L))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // when
        List<NotificationResponse> responses = notificationService.getMemberNotifications(1L, null);
        
        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getStatus()).isEqualTo(NotificationStatus.SENT.name());
        verify(notificationQueueRepository, times(1)).findByMemberId(1L);
        verify(notificationQueueRepository, times(0)).findByMemberIdAndStatus(anyLong(), anyString());
    }
    
    @DisplayName("회원의 알림 목록을 상태 필터링하여 조회한다")
    @Test
    void getMemberNotifications_withStatus() {
        // given
        NotificationTypes type = NotificationTypes.builder().id(1L).type("RESERVATION_CONFIRM").title("예약 확인").build();
        NotificationQueue queue = NotificationQueue.builder()
                .id(1L)
                .notificationTypes(type)
                .memberId(1L)
                .scheduledAt(LocalDateTime.now())
                .status(NotificationStatus.SENT.name())
                .build();
        when(notificationQueueRepository.findByMemberIdAndStatus(1L, NotificationStatus.SENT.name()))
                .thenReturn(List.of(queue));
        when(notificationLogRepository.findByNotificationQueueIdOrderBySentAtDesc(1L))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        // when
        List<NotificationResponse> responses = notificationService.getMemberNotifications(1L,
                NotificationStatus.SENT.name());
        
        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getStatus()).isEqualTo(NotificationStatus.SENT.name());
        verify(notificationQueueRepository, times(1)).findByMemberIdAndStatus(1L, NotificationStatus.SENT.name());
        verify(notificationQueueRepository, times(0)).findByMemberId(anyLong());
    }
}