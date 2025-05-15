package com.goorm.tablepick.global.config;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.notification.constant.NotificationStatus;
import com.goorm.tablepick.domain.notification.entity.NotificationQueue;
import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationQueueRepository;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("local") // 로컬 환경에서만 실행되도록 설정
public class TestDataInitializer implements ApplicationRunner {
    private final NotificationTypesRepository notificationTypesRepository;
    private final NotificationQueueRepository notificationQueueRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 테스트용 회원 생성 (이미 있다면 생성하지 않음)
        Member testMember = memberRepository.findById(1L).orElseGet(() ->
                memberRepository.save(Member.builder()
                        .email("test@example.com")
                        .nickname("테스트사용자")
                        .fcmToken("test-fcm-token")
                        .isMemberDeleted(false)
                        .build())
        );

        // 알림 타입 생성 (NotificationTypeInitializer에서 이미 생성되었을 수 있음)
        NotificationTypes type1Day = notificationTypesRepository.findByType("RESERVATION_1DAY_BEFORE")
                .orElseThrow(() -> new RuntimeException("Notification type not found"));
        NotificationTypes type3Hours = notificationTypesRepository.findByType("RESERVATION_3HOURS_BEFORE")
                .orElseThrow(() -> new RuntimeException("Notification type not found"));
        NotificationTypes type1Hour = notificationTypesRepository.findByType("RESERVATION_1HOUR_BEFORE")
                .orElseThrow(() -> new RuntimeException("Notification type not found"));

        // 테스트용 알림 큐 생성
        if (notificationQueueRepository.count() == 0) {
            // 예정된 알림
            createNotificationQueue(testMember.getId(), 1L, type1Day,
                    LocalDateTime.now().plusDays(1), NotificationStatus.PENDING);
            createNotificationQueue(testMember.getId(), 1L, type3Hours,
                    LocalDateTime.now().plusHours(3), NotificationStatus.PENDING);
            createNotificationQueue(testMember.getId(), 1L, type1Hour,
                    LocalDateTime.now().plusHours(1), NotificationStatus.PENDING);

            // 이미 발송된 알림
            createNotificationQueue(testMember.getId(), 2L, type1Day,
                    LocalDateTime.now().minusDays(1), NotificationStatus.SENT);
            createNotificationQueue(testMember.getId(), 2L, type3Hours,
                    LocalDateTime.now().minusHours(3), NotificationStatus.SENT);

            // 실패한 알림
            createNotificationQueue(testMember.getId(), 3L, type1Hour,
                    LocalDateTime.now().minusHours(1), NotificationStatus.FAILED);

            log.info("Test notification queues created");
        }
    }

    private void createNotificationQueue(
            Long memberId,
            Long reservationId,
            NotificationTypes type,
            LocalDateTime scheduledAt,
            NotificationStatus status
    ) {
        NotificationQueue queue = NotificationQueue.builder()
                .memberId(memberId)
                .reservationId(reservationId)
                .notificationTypes(type)
                .scheduledAt(scheduledAt)
                .status(status.name())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        notificationQueueRepository.save(queue);
    }
}
