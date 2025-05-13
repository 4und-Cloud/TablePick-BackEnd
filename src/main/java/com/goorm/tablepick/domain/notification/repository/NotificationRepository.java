package com.goorm.tablepick.domain.notification.repository;

import com.goorm.tablepick.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Notification 엔티티에 대한 레포지토리 인터페이스
 * 기본 CRUD 메서드는 JpaRepository에서 제공됨
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * 회원 ID로 알림 목록을 조회합니다.
     *
     * @param memberId 회원 ID
     * @return 해당 회원의 알림 목록
     */
    List<Notification> findByMemberId(Long memberId);

    /**
     * 예약 ID로 알림 목록을 조회합니다.
     *
     * @param reservationId 예약 ID
     * @return 해당 예약과 관련된 알림 목록
     */
    List<Notification> findByReservationId(Long reservationId);

    /**
     * 알림 유형별로 알림 목록을 조회합니다.
     *
     * @param type 알림 유형
     * @return 해당 유형의 알림 목록
     */
    List<Notification> findByType(String type);
}
