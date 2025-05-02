package com.goorm.tablepick.domain.reservation.repository;

import com.goorm.tablepick.domain.reservation.entity.ReservationTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
}
