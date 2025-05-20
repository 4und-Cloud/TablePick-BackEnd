package com.goorm.tablepick.domain.reservation.repository;

import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByReservationSlot(ReservationSlot reservationSlot);

    @Query("SELECT r FROM Reservation r WHERE r.reservationStatus != 'CANCELLED'")
    List<Reservation> findAllByMemberEmail(String username);

    // 특정 시간 범위 내의 예약을 조회하는 메서드 (알림 스케줄링용)
    // ReservationSlot의 date와 time을 조합하여 시간 범위를 확인
    @Query("SELECT r FROM Reservation r " +
            "JOIN r.reservationSlot rs " +
            "WHERE CONCAT(rs.date, ' ', rs.time) BETWEEN :startTime AND :endTime")
    List<Reservation> findByReservationDateTimeBetween(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 회원 ID로 예약을 조회하는 메서드
    List<Reservation> findByMemberId(Long memberId);
}
