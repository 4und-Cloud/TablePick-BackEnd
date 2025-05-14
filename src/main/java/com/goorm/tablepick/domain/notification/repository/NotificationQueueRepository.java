package com.goorm.tablepick.domain.notification.repository;

import com.goorm.tablepick.domain.notification.entity.NotificationQueue;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, Long> {
    List<NotificationQueue> findByStatusAndScheduledAtBefore(String status, LocalDateTime time);

    List<NotificationQueue> findByMemberId(Long memberId);

    List<NotificationQueue> findByMemberIdAndStatus(Long memberId, String status);

}
