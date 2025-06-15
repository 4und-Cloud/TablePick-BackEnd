package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
@ExtendWith(OutputCaptureExtension.class)
@SpringBootTest
@Transactional
class RegisterNotificationServiceImplIntegTest {
    
    @Autowired
    private RegisterNotificationService registerNotificationService;
    
    @Autowired
    private NotificationTypesRepository notificationTypesRepository;
    
    @MockBean
    private NotificationService notificationService;
    
    @MockBean
    private FCMTokenService fcmTokenService;
    
    @BeforeEach
    void setUp() {
        notificationTypesRepository.deleteAllInBatch();
    }
    
    @AfterEach
    void tearDown() {
        reset(notificationService, fcmTokenService);
    }
    
    @DisplayName("새로운 회원에게 환영 알림을 성공적으로 예약한다.")
    @Test
    void sendWelcomeNotificationForNewMember() {
        // given
        NotificationTypes notificationType = NotificationTypes.builder()
                .type(NotificationTypes.REGISTER_COMPLETED)
                .title("환영합니다!")
                .body("TablePick에 가입해주셔서 감사합니다.")
                .build();
        notificationTypesRepository.save(notificationType);
        
        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now().minusSeconds(30))
                .build();
        
        when(fcmTokenService.getFcmToken(1L)).thenReturn("valid-fcm-token");
        
        // when
        registerNotificationService.sendWelcomeNotification(member);
        
        // then
        verify(fcmTokenService, times(1)).getFcmToken(1L);
        verify(notificationService, times(1)).scheduleNotification(any());
    }
    
    @DisplayName("1분 이상 지난 회원에게는 환영 알림을 보내지 않는다.")
    @Test
    void skipWelcomeNotificationForOldMember(CapturedOutput capturedOutput) {
        // given
        Member member = Member.builder()
                .id(1L)
                .createdAt(LocalDateTime.now().minusMinutes(2))
                .build();
        
        // when
        registerNotificationService.sendWelcomeNotification(member);
        
        // then
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
        verify(fcmTokenService, never()).getFcmToken(any());
        verify(notificationService, never()).scheduleNotification(any());
        assertThat(capturedOutput.getOut()).contains("Member createdAt is null");
    }
}