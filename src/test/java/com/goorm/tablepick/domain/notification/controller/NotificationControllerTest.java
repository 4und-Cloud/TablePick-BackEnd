package com.goorm.tablepick.domain.notification.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goorm.tablepick.domain.notification.dto.request.FCMTokenRequest;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.dto.response.NotificationResponse;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import com.goorm.tablepick.domain.notification.service.FCMTokenService;
import com.goorm.tablepick.domain.notification.service.NotificationService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private FCMTokenService fcmTokenService;
    
    @Mock
    private NotificationTypesRepository notificationTypesRepository;
    
    @InjectMocks
    private NotificationController notificationController;
    
    private NotificationRequest notificationRequest;
    private NotificationResponse notificationResponse;
    private FCMTokenRequest fcmTokenRequest;
    private NotificationTypes notificationType;
    
    @BeforeEach
    void setUp() {
        notificationRequest = NotificationRequest.builder()
                .memberId(1L)
                .notificationTypeId(1L)
                .scheduledAt(LocalDateTime.now())
                .build();
        
        notificationResponse = NotificationResponse.builder()
                .id(1L)
                .memberId(1L)
                .scheduledAt(LocalDateTime.now())
                .status("PENDING")
                .build();
        
        fcmTokenRequest = new FCMTokenRequest("test-token");
        
        notificationType = NotificationTypes.builder()
                .id(1L)
                .type("TEST_NOTIFICATION")
                .title("테스트 알림")
                .body("테스트 알림 내용")
                .url("https://tablepick.com")
                .build();
    }
    
    @Test
    @DisplayName("유효한 알림 요청으로 알림 예약이 성공적으로 처리된다")
    void scheduleNotification_withValidRequest_succeeds() {
        // given 준비
        when(notificationService.scheduleNotification(any(NotificationRequest.class)))
                .thenReturn(notificationResponse); // 유효한 알림 요청
        
        // when 실행
        ResponseEntity<NotificationResponse> response = notificationController
                .scheduleNotification(notificationRequest); // 알림 예약 API 호출
        
        // then 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notificationResponse, response.getBody());
        verify(notificationService).scheduleNotification(notificationRequest);
    }
    
    @Test
    @DisplayName("유효한 알림 ID로 알림 상태 조회가 성공적으로 처리된다")
    void getNotificationStatus_withValidId_succeeds() {
        // given 준비
        when(notificationService.getNotificationStatus(anyLong()))
                .thenReturn(notificationResponse); // 유효한 알림 id 요청
        
        // when 실행
        ResponseEntity<NotificationResponse> response = notificationController
                .getNotificationStatus(1L); // 알림 상태 조회 API 호출
        
        // then 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notificationResponse, response.getBody());
        verify(notificationService).getNotificationStatus(1L);
    }
    
    @Test
    @DisplayName("유효한 회원 ID와 상태로 회원 알림 목록 조회가 성공적으로 처리된다")
    void getMemberNotifications_withValidMemberIdAndStatus_succeeds() {
        // given 준비
        List<NotificationResponse> notifications = Arrays.asList(notificationResponse);
        when(notificationService.getMemberNotifications(anyLong(), any()))
                .thenReturn(notifications); // 회원 ID와 상태로 알림 목록 준비
        
        // when 실행
        ResponseEntity<List<NotificationResponse>> response = notificationController
                .getMemberNotifications(1L, "SENT"); // 회원 알림 목록 조회 API 호출
        
        // then 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notifications, response.getBody());
        verify(notificationService).getMemberNotifications(1L, "SENT");
    }
    
    @Test
    @DisplayName("유효한 FCM 토큰으로 회원의 FCM 토큰 업데이트가 성공적으로 처리된다")
    void updateFcmToken_withValidToken_succeeds() {
        // given 준비
        doNothing().when(fcmTokenService).updateFcmToken(anyLong(), anyString()); // 유효한 회원 ID와 FCM 토큰 준비
        
        // when 실행
        ResponseEntity<Void> response = notificationController
                .updateFcmToken(1L, fcmTokenRequest); // FCM 토큰 업데이트 API 호출
        
        // then 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(fcmTokenService).updateFcmToken(1L, "test-token");
    }
    
    @Test
    @DisplayName("유효한 회원 ID로 FCM 토큰을 NULL로 변경이 성공적으로 처리된다")
    void removeFcmToken_withValidMemberId_succeeds() {
        // given 준비
        doNothing().when(fcmTokenService).updateFcmTokenToNull(anyLong()); // 유효한 회원 ID 준비
        
        // when 실행
        ResponseEntity<Void> response = notificationController
                .removeFcmToken(1L); // FCM 토큰 제거 API 호출
        
        // then 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(fcmTokenService).updateFcmTokenToNull(1L);
    }
    
    @Test
    @DisplayName("유효한 회원 ID와 알림 타입 ID로 테스트 알림 전송이 성공적으로 처리된다")
    void sendTestNotification_withValidIds_succeeds() {
        // given 준비
        when(notificationService.scheduleNotification(any(NotificationRequest.class)))
                .thenReturn(notificationResponse); // 유효한 회원 ID와 알림 타입 ID 준비
        
        // when 실행
        ResponseEntity<NotificationResponse> response = notificationController
                .sendTestNotification(1L, 1L); // 테스트 알림 전송 API 호출
        
        // then 실행
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(notificationResponse, response.getBody());
        verify(notificationService).scheduleNotification(any(NotificationRequest.class));
    }
    
    @Test
    @DisplayName("알림 타입 목록 조회가 성공적으로 처리된다")
    void getNotificationTypes_succeeds() {
        // given 준비
        List<NotificationTypes> types = Arrays.asList(notificationType);
        when(notificationTypesRepository.findAll()).thenReturn(types); // 알림 타입 목록 준비
        
        // when 실행
        ResponseEntity<List<NotificationTypes>> response = notificationController
                .getNotificationTypes(); // 알림 타입 목록 조회 API 호출
        
        // then 검증
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(types, response.getBody());
        verify(notificationTypesRepository).findAll();
    }
}