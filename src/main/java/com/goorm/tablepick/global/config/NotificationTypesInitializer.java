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
                "회원가입 완료",
                "테이블픽에 가입해 주셔서 감사합니다. 앞으로 다양한 혜택과 서비스를 이용해보세요.",
                ""
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_COMPLETED,
                "식당 예약 완료",
                "{restaurantName} 예약이 성공적으로 완료되었습니다.",
                "/reservations/{id}"
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_1DAY_BEFORE,
                "식당 예약날 1일 전",
                "내일 이 시간에 {restaurantName} 예약이 예정되어 있습니다.",
                "/reservations/{id}"
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_3HOURS_BEFORE,
                "식당 예약시간 3시간 전",
                "3시간 뒤 {restaurantName} 예약이 있습니다. 위치와 시간을 다시 한번 확인해 주세요.",
                "/reservations/{id}"
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_1HOUR_BEFORE,
                "식당 예약시간 1시간 전",
                "{restaurantName} 예약이 1시간 남았습니다. 매장에서 고객님을 맞을 준비를 하고 있으니 여유 있게 출발해주세요.",
                "/reservations/{id}"
        );

        ensureNotificationTypeExists(
                NotificationTypes.RESERVATION_3HOURS_AFTER,
                "식당 리뷰 요청",
                "{restaurantName}에서의 식사는 만족스러우셨나요? 리뷰를 남겨주시면 큰 도움이 됩니다.",
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
