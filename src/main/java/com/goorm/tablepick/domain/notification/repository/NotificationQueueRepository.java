package com.goorm.tablepick.domain.notification.repository;

import com.goorm.tablepick.domain.notification.entity.NotificationQueue;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
// 알림 큐 엔티티에 대한 데이터 액세스 인터페이스
public interface NotificationQueueRepository extends JpaRepository<NotificationQueue, Long> {

    // 특정 상태이며 지정된 시간 이전에 예약된 알림 목록을 조회
    List<NotificationQueue> findByStatusAndScheduledAtBefore(String status, LocalDateTime time);

    // 특정 회원의 알림 큐 목록을 조회
    List<NotificationQueue> findByMemberId(Long memberId);

    // 특정 회원의 특정 상태의 알림 큐 목록을 조회
    List<NotificationQueue> findByMemberIdAndStatus(Long memberId, String status);

    // 특정 회원, 예약, 알림 타입에 대해 지정된 상태의 알림이 존재하는지 확인
    boolean existsByMemberIdAndReservationIdAndNotificationTypes_IdAndStatusIn(
            Long memberId, Long reservationId, Long notificationTypesId, List<String> statusList);
}
