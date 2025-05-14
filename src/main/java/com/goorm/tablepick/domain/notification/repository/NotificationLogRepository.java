package com.goorm.tablepick.domain.notification.repository;

import com.goorm.tablepick.domain.notification.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {
}
