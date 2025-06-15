package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.global.exception.NotificationException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class FCMTokenServiceImplTest {
    
    @MockBean
    private MemberRepository memberRepository;
    
    @Autowired
    private FCMTokenService fcmTokenService;
    
    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("회원 ID가 유효하면 FCM 토큰이 성공적으로 업데이트가 된다.")
    void updateFcmToken() {
        // given 준비
        Long memberId = 1L;
        String fcmToken = "fcm-token";
        Member mockMember = getMember(memberId);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        
        // when 실행
        fcmTokenService.updateFcmToken(memberId, fcmToken);
        
        // then 검증
        verify(memberRepository, times(1)).findById(memberId);
        assertThat(mockMember.getFcmToken()).isEqualTo(fcmToken);
    }
    
    @Test
    @DisplayName("회원의 FCM 토큰이 성공적으로 null로 변경된다.")
    void updateFcmTokenToNull() {
        // given 준비
        Long memberId = 1L;
        String fcmToken = "fcm-token";
        Member mockMember = getMember(memberId);
        mockMember.updateFcmToken(fcmToken); // 초기 토큰 설정
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        
        // when 실행
        fcmTokenService.updateFcmTokenToNull(memberId);
        
        // then 검증
        verify(memberRepository, times(1)).findById(memberId);
        assertThat(mockMember.getFcmToken()).isNull();
    }
    
    @Test
    @DisplayName("회원 ID가 유효하면 FCM 토큰이 성공적으로 조회된다.")
    void getFcmToken() {
        // given 준비
        Long memberId = 1L;
        String expectedFcmToken = "expected-fcm-token";
        Member mockMember = getMember(memberId);
        mockMember.updateFcmToken(expectedFcmToken);
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        
        // when 실행
        String fcmToken = fcmTokenService.getFcmToken(memberId);
        
        // then 검증
        verify(memberRepository, times(1)).findById(memberId);
        assertThat(fcmToken).isEqualTo(expectedFcmToken);
    }
    
    @Test
    @DisplayName("존재하지 않는 회원 ID로 FCM 토큰 업데이트 시 예외가 발생한다.")
    void updateFcmTokenWithInvalidMemberId() {
        // given 준비
        Long memberId = 999L;
        String fcmToken = "fcm-token";
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> fcmTokenService.updateFcmToken(memberId, fcmToken))
                .isInstanceOf(NotificationException.class)
                .hasMessage("Member not found");
        
        verify(memberRepository, times(1)).findById(memberId);
    }
    
    @Test
    @DisplayName("존재하지 않는 회원 ID로 FCM 토큰을 null로 변경 시 예외가 발생한다.")
    void updateFcmTokenToNullWithInvalidMemberId() {
        // given 준비
        Long memberId = 999L;
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> fcmTokenService.updateFcmTokenToNull(memberId))
                .isInstanceOf(NotificationException.class)
                .hasMessage("Member not found");
        
        verify(memberRepository, times(1)).findById(memberId);
    }
    
    @Test
    @DisplayName("존재하지 않는 회원 ID로 FCM 토큰을 조회하면 null을 반환한다.")
    void getFcmTokenWithInvalidMemberId() {
        // given 준비
        Long memberId = 999L;
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());
        
        // when 실행
        String fcmToken = fcmTokenService.getFcmToken(memberId);
        
        // then 검증
        verify(memberRepository, times(1)).findById(memberId);
        assertThat(fcmToken).isNull();
    }
    
    @Test
    @DisplayName("FCM 토큰이 null이거나 빈 문자열인 경우 null을 반환한다.")
    void getFcmTokenWhenTokenIsNullOrEmpty() {
        // given 준비
        Long memberId = 1L;
        Member mockMember = getMember(memberId);
        mockMember.updateFcmToken("");
        
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        
        // when 실행
        String fcmToken = fcmTokenService.getFcmToken(memberId);
        
        // then 검증
        verify(memberRepository, times(1)).findById(memberId);
        assertThat(fcmToken).isNull();
    }
    
    private static Member getMember(Long memberId) {
        return Member.builder()
                .id(memberId)
                .email("test@example.com")
                .build();
    }
    
}