package com.goorm.tablepick.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.global.exception.NotificationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class FCMTokenServiceImplIntegTest {
    
    @Autowired
    private FCMTokenService fcmTokenService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @AfterEach
    void tearDown() {
        memberRepository.deleteAllInBatch();
    }
    
    @Test
    @DisplayName("회원 ID가 유효하면 FCM 토큰이 성공적으로 업데이트가 된다.")
    void updateFcmToken() {
        // given 준비
        Member member = createAndSaveMember("test@example.com"); // 회원 추가 및 저장
        String fcmToken = "fcm-token"; // FCM 토큰 생성
        
        // when 실행
        fcmTokenService.updateFcmToken(member.getId(), fcmToken); // FCM 토큰 업데이트
        
        // then 검증
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow(); // 업데이트된 회원 정보 가져오기
        assertThat(updatedMember.getFcmToken()).isEqualTo(fcmToken); // 업데이트된 FCM 토큰과 비교
    }
    
    @Test
    @DisplayName("회원의 FCM 토큰이 성공적으로 null로 업데이트가 된다.")
    void updateFcmTokenToNull() {
        // given 준비
        Member member = createAndSaveMember("test@example.com");
        String fcmToken = "fcm-token";
        
        fcmTokenService.updateFcmToken(member.getId(), fcmToken);
        
        Member memberWithToken = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(memberWithToken.getFcmToken()).isEqualTo(fcmToken);
        
        // when 실행
        fcmTokenService.updateFcmTokenToNull(member.getId()); // FCM 토큰 업데이트(null로 변경)
        
        // then 검증
        Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(updatedMember.getFcmToken()).isNull(); // 업데이트된 FCM 토큰이 null인지 확인
    }
    
    @Test
    @DisplayName("회원 ID가 유효하면 FCM 토큰이 성공적으로 조회된다.")
    void getFcmToken() {
        // given 준비
        Member member = createAndSaveMember("test@example.com");
        String fcmToken = "fcm-token";
        
        fcmTokenService.updateFcmToken(member.getId(), fcmToken);
        
        // when 실행
        String actualFcmToken = fcmTokenService.getFcmToken(member.getId());
        
        // then 검증
        assertThat(actualFcmToken).isEqualTo(fcmToken); // 실제 FCM 토큰과 기대값 비교
    }
    
    @Test
    @DisplayName("존재하지 않는 회원 ID로 FCM 토큰 업데이트 시 예외가 발생한다.")
    void updateFcmTokenWithInvalidMemberId() {
        // given 준비
        Long memberId = 999L; // 존재하지 않는 회원 ID
        String fcmToken = "fcm-token";
        
        // when & then
        assertThatThrownBy(() -> fcmTokenService.updateFcmToken(memberId, fcmToken)) // 예외 발생 여부 확인
                .isInstanceOf(NotificationException.class)
                .hasMessage("Member not found"); // 예외 메시지와 일치하는지 확인
    }
    
    @Test
    @DisplayName("존재하지 않는 회원 ID로 FCM 토큰 조회 시 null을 반환한다.")
    void getFcmTokenWithInvalidMemberId() {
        // given 준비
        Long memberId = 999L;
        
        // when 실행
        String fcmToken = fcmTokenService.getFcmToken(memberId);
        
        // then 검증
        assertThat(fcmToken).isNull(); // 결과가 null인지 확인
    }
    
    @Test
    @DisplayName("FCM 토큰이 null이거나 빈 문자열인 경우 null을 반환한다.")
    void getFcmTokenWhenTokenIsNullOrEmpty() {
        // given 준비
        Member member = createAndSaveMember("test@example.com");
        
        // when 실행
        String actualFcmToken = fcmTokenService.getFcmToken(member.getId());
        
        // then 검증
        assertThat(actualFcmToken).isNull();
        
        fcmTokenService.updateFcmToken(member.getId(), ""); // FCM 토큰 null로 변경
        String emptyTokenResult = fcmTokenService.getFcmToken(member.getId());
        assertThat(emptyTokenResult).isNull();
    }
    
    @Test
    @DisplayName("FCM 토큰 업데이트 후 다시 업데이트하면 새로운 토큰으로 변경된다.")
    void updateFcmTokenMultipleTimes() {
        // given 준비
        Member member = createAndSaveMember("test@example.com");
        String fcmToken1 = "fcm-token-1";
        String fcmToken2 = "fcm-token-2";
        
        // when 실행
        fcmTokenService.updateFcmToken(member.getId(), fcmToken1); // fcmToken1로 업데이트
        String result1 = fcmTokenService.getFcmToken(member.getId());
        
        fcmTokenService.updateFcmToken(member.getId(), fcmToken2); // fcmToken2로 업데이트
        String result2 = fcmTokenService.getFcmToken(member.getId());
        
        // then 검증
        assertThat(result1).isEqualTo(fcmToken1);
        assertThat(result2).isEqualTo(fcmToken2);
        assertThat(result2).isNotEqualTo(result1);
    }
    
    private Member createAndSaveMember(String email) {
        Member member = Member.builder()
                .email(email)
                .nickname("testUser")
                .roles(AccountRole.USER)
                .provider("test")
                .providerId("test123")
                .isMemberDeleted(false)
                .build();
        
        return memberRepository.save(member);
    }
}