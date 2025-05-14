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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FCMTokenServiceTest {

    @InjectMocks
    private FCMTokenService fcmTokenService;

    @Mock
    private MemberRepository memberRepository;

    private Member testMember;
    private final Long memberId = 1L;
    private final String fcmToken = "test-fcm-token";

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(memberId)
                .build();
    }

    @Test
    @DisplayName("FCM 토큰 업데이트 성공")
    void updateFcmTokenSuccess() {
        // given
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));

        // when
        fcmTokenService.updateFcmToken(memberId, fcmToken);

        // then
        assertThat(testMember.getFcmToken()).isEqualTo(fcmToken);
        verify(memberRepository).findById(memberId);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 FCM 토큰 업데이트 시도")
    void updateFcmTokenMemberNotFound() {
        // given
        given(memberRepository.findById(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fcmTokenService.updateFcmToken(memberId, fcmToken))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("FCM 토큰 삭제 성공")
    void deleteFcmTokenSuccess() {
        // given
        testMember.updateFcmToken(fcmToken);
        given(memberRepository.findById(memberId)).willReturn(Optional.of(testMember));

        // when
        fcmTokenService.deleteFcmToken(memberId);

        // then
        assertThat(testMember.getFcmToken()).isNull();
        verify(memberRepository).findById(memberId);
    }
}
