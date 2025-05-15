package com.goorm.tablepick.global.config;


import com.goorm.tablepick.domain.notification.constant.NotificationTypes;
import com.goorm.tablepick.domain.notification.repository.NotificationTypesRepository;
import java.util.Arrays;
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
        if (notificationTypesRepository.count() == 0) {
            Arrays.stream(NotificationTypes.values()).forEach(type -> {
                com.goorm.tablepick.domain.notification.entity.NotificationTypes entity =
                        com.goorm.tablepick.domain.notification.entity.NotificationTypes.builder()
                                .type(type.name())
                                .title(type.getTitle())
                                .body(type.getBodyTemplate())
                                .url(type.getUrl())
                                .build();
                notificationTypesRepository.save(entity);
            });
            log.info("Notification types initialized");
        }
    }
}
