package com.goorm.tablepick.global.config;

import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTypesInitializer implements ApplicationRunner {
    private final NotificationTypesRepository notificationTypesRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // 항상 모든 알림 타입을 확인하고 필요한 경우 생성
        ensureNotificationTypeExists(
                NotificationTypes.REGISTER_COMPLETED,
                "회원가입 완료 알림",
                "저희 테이블픽의 회원이 되신 것을 진심으로 환영합니다!",
                ""
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_COMPLETED,
                "예약 완료 알림",
                "{restaurantName} 예약이 성공적으로 완료되었습니다!",
                "/reservations/{id}"
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_1DAY_BEFORE,
                "예약 1일 전 알림",
                "내일 이 시간에 {restaurantName} 예약이 있습니다!",
                "/reservations/{id}"
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_3HOURS_BEFORE,
                "예약 3시간 전 알림",
                "3시간 뒤에 {restaurantName} 예약이 있습니다! 까먹지 않게 또 알려드릴게요!",
                "/reservations/{id}"
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_1HOUR_BEFORE,
                "예약 1시간 전 알림",
                "예약하신 {restaurantName}이 열심히 준비하고 있습니다! 1시간 뒤에 늦지 않게 입장해주세용~!",
                "/reservations/{id}"
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_3HOURS_AFTER,
                "예약 3시간 후 알림",
                "{restaurantName} 식사는 어떠셨나요? 리뷰를 남겨주시면 다음 손님들에게 큰 도움이 됩니다!",
                "/reservations/{id}/review"
        );

        log.info("알림 타입 초기화 완료");
    }

    private void ensureNotificationTypeExists(String type, String title, String body, String url) {
        // 해당 타입의 알림이 존재하는지 확인
        if (!notificationTypesRepository.existsByType(type)) {
            // 존재하지 않으면 생성
            createNotificationType(type, title, body, url);
            log.info("새 알림 타입 추가: {}", type);
        }
    }

    private void createNotificationType(String type, String title, String body, String url) {
        NotificationTypes entity = NotificationTypes.builder()
                .type(type)
                .title(title)
                .body(body)
                .url(url)
                .build();
        notificationTypesRepository.save(entity);
        log.info("알림 타입 생성: {}", type);
    }
}
