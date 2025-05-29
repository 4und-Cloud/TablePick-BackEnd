package com.goorm.tablepick.domain.notification.repository;

import com.goorm.tablepick.domain.notification.entity.NotificationLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByNotificationQueueIdOrderBySentAtDesc(Long notificationQueueId);
}
