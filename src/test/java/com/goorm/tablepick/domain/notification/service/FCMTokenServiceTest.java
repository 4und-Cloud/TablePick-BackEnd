package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.global.exception.NotificationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FCMTokenServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private FCMTokenService fcmTokenService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = mock(Member.class);
        when(testMember.getId()).thenReturn(1L);
    }

    @Test
    @DisplayName("FCM 토큰 업데이트 테스트")
    void updateFcmToken_ShouldUpdateToken() {
        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        doNothing().when(testMember).updateFcmToken(anyString());

        // When
        fcmTokenService.updateFcmToken(1L, "new-fcm-token");

        // Then
        verify(memberRepository, times(1)).findById(1L);
        verify(testMember, times(1)).updateFcmToken("new-fcm-token");
    }

    @Test
    @DisplayName("존재하지 않는 회원의 FCM 토큰 업데이트 테스트")
    void updateFcmToken_WithNonExistingMember_ShouldThrowException() {
        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            fcmTokenService.updateFcmToken(999L, "new-fcm-token");
        });

        assertEquals("Member not found", exception.getMessage());
        assertEquals("MEMBER_NOT_FOUND", exception.getErrorCode());
        verify(memberRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("FCM 토큰 삭제 테스트")
    void deleteFcmToken_ShouldRemoveToken() {
        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        doNothing().when(testMember).removeFcmToken();

        // When
        fcmTokenService.deleteFcmToken(1L);

        // Then
        verify(memberRepository, times(1)).findById(1L);
        verify(testMember, times(1)).removeFcmToken();
    }

    @Test
    @DisplayName("FCM 토큰 조회 테스트")
    void getFcmToken_ShouldReturnToken() {
        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        when(testMember.getFcmToken()).thenReturn("test-fcm-token");

        // When
        String token = fcmTokenService.getFcmToken(1L);

        // Then
        assertEquals("test-fcm-token", token);
        verify(memberRepository, times(1)).findById(1L);
        verify(testMember, times(1)).getFcmToken();
    }

    @Test
    @DisplayName("FCM 토큰이 없는 회원의 토큰 조회 테스트")
    void getFcmToken_WithNoToken_ShouldThrowException() {
        // Given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        when(testMember.getFcmToken()).thenReturn(null);

        // When & Then
        NotificationException exception = assertThrows(NotificationException.class, () -> {
            fcmTokenService.getFcmToken(1L);
        });

        assertEquals("FCM token not found", exception.getMessage());
        assertEquals("TOKEN_NOT_FOUND", exception.getErrorCode());
        verify(memberRepository, times(1)).findById(1L);
        verify(testMember, times(1)).getFcmToken();
    }
}
