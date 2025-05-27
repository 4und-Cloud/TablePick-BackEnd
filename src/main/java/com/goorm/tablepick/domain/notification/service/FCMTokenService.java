package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.global.exception.NotificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class FCMTokenService {
    private final MemberRepository memberRepository;

    // FCM 토큰 부분 업데이트
    public void updateFcmToken(Long memberId, String fcmToken) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotificationException("Member not found", "MEMBER_NOT_FOUND"));

        member.updateFcmToken(fcmToken);
        log.info("Updated FCM token for member: {}", memberId);
    }

    // FCM 토큰 삭제
    public void updateFcmTokenToNull(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotificationException("Member not found", "MEMBER_NOT_FOUND"));

        member.removeFcmToken();
        log.info("Deleted FCM token for member: {}", memberId);
    }

    // FCM 토큰 조회
    @Transactional(readOnly = true)
    public String getFcmToken(Long memberId) {
        try {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new NotificationException("Member not found", "MEMBER_NOT_FOUND"));

            String fcmToken = member.getFcmToken();
            log.debug("FCM Token for member {}: {}", memberId, fcmToken != null ? "Found" : "Not found");
            
            // 토큰이 없는 경우 null 반환 (예외 발생하지 않음)
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.warn("FCM token not found for member ID: {}", memberId);
                return null;
            }

            return fcmToken;
        } catch (Exception e) {
            log.error("Error retrieving FCM token for member ID {}: {}", memberId, e.getMessage());
            return null;
        }
    }
}
