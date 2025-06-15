package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.dto.request.NotificationRequest;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
public class RegisterNotificationServiceImplTest {
    
    @Mock
    private NotificationService notificationService;
    
    @Mock
    private NotificationTypesRepository notificationTypesRepository;
    
    @Mock
    private FCMTokenService fcmTokenService;
    
    @InjectMocks
    private RegisterNotificationServiceImpl registerNotificationService;
    
    @AfterEach
    void tearDown() {
        reset(notificationService, notificationTypesRepository, fcmTokenService);
    }
    
    @DisplayName("새로운 회원에게 환영 알림을 성공적으로 예약한다.")
    @Test
    void sendWelcomeNotificationForNewMember() {
        // given
        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now().minusSeconds(30)) // 30초 전 생성
                .build();
        NotificationTypes notificationType = NotificationTypes.builder()
                .id(1L)
                .type(NotificationTypes.REGISTER_COMPLETED)
                .build();
        
        when(notificationTypesRepository.findByType(NotificationTypes.REGISTER_COMPLETED))
                .thenReturn(Optional.of(notificationType));
        when(fcmTokenService.getFcmToken(1L)).thenReturn("valid-fcm-token");
        
        // when
        registerNotificationService.sendWelcomeNotification(member);
        
        // then
        verify(notificationTypesRepository, times(1)).findByType(NotificationTypes.REGISTER_COMPLETED);
        verify(fcmTokenService, times(1)).getFcmToken(1L);
        verify(notificationService, times(1)).scheduleNotification(any(NotificationRequest.class));
    }
    
    @DisplayName("1분 이상 지난 회원에게는 환영 알림을 보내지 않는다.")
    @Test
    void skipWelcomeNotificationForOldMember(CapturedOutput capturedOutput) {
        // given
        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now().minusMinutes(2)) // 2분 전 생성
                .build();
        
        // when
        registerNotificationService.sendWelcomeNotification(member);
        
        // then
        verify(notificationTypesRepository, never()).findByType(any());
        verify(fcmTokenService, never()).getFcmToken(any());
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("Member is not new");
    }
    
    @DisplayName("멤버의 생성 시간이 null이면 환영 알림을 보내지 않는다.")
    @Test
    void skipWelcomeNotificationForNullCreatedAt(CapturedOutput capturedOutput) {
        // given
        Member member = Member.builder()
                .id(1L)
                .createdAt(null)
                .build();
        
        // when
        registerNotificationService.sendWelcomeNotification(member);
        
        // then
        verify(notificationTypesRepository, never()).findByType(any());
        verify(fcmTokenService, never()).getFcmToken(any());
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("Member createdAt is null");
    }
    
    @DisplayName("FCM 토큰이 없으면 최대 5번 재시도 후 실패 로그를 남긴다.")
    @Test
    void retryScheduleWelcomeNotificationWhenNoFcmToken(CapturedOutput capturedOutput) throws InterruptedException {
        // given
        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now().minusSeconds(30))
                .build();
        NotificationTypes notificationType = NotificationTypes.builder()
                .id(1L)
                .type(NotificationTypes.REGISTER_COMPLETED)
                .build();
        
        when(notificationTypesRepository.findByType(NotificationTypes.REGISTER_COMPLETED))
                .thenReturn(Optional.of(notificationType));
        when(fcmTokenService.getFcmToken(1L)).thenReturn(null);
        
        // when
        registerNotificationService.sendWelcomeNotification(member);
        Thread.sleep(6000); // 5 retries * 1 second delay + buffer
        
        // then
        verify(fcmTokenService, times(6)).getFcmToken(1L); // Initial + 5 retries
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("Failed to schedule welcome notification");
    }
    
    @DisplayName("알림 타입이 없으면 예외가 발생한다.")
    @Test
    void throwExceptionWhenNotificationTypeNotFound() {
        // given
        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now().minusSeconds(30))
                .build();
        
        when(notificationTypesRepository.findByType(NotificationTypes.REGISTER_COMPLETED))
                .thenReturn(Optional.empty());
        
        // when & then
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> registerNotificationService.sendWelcomeNotification(member)
        );
        assertThat(exception.getMessage()).isEqualTo("Register completed notification type not found");
        verify(fcmTokenService, never()).getFcmToken(any());
        verify(notificationService, never()).scheduleNotification(any());
    }
}