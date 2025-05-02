package com.goorm.tablepick.domain.reservation.repository;

import com.goorm.tablepick.domain.reservation.entity.ReservationTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    @Query("""
            SELECT rt FROM ReservationTime rt
            WHERE rt.restaurantOperatingHour.id = :operatingHourId
            AND rt.isOpen = true
            AND rt.count < 3
            """)
    List<ReservationTime> findAvailableTimes(@Param("operatingHourId") Long operatingHourId);

}
