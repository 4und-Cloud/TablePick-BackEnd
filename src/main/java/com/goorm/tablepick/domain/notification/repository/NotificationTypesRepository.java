package com.goorm.tablepick.domain.notification.repository;

import com.goorm.tablepick.domain.notification.entity.NotificationTypes;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationTypesRepository extends JpaRepository<NotificationTypes, Long> {
    Optional<NotificationTypes> findByType(String type);
}
