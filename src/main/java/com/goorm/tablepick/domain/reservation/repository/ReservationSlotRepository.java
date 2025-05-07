package com.goorm.tablepick.domain.reservation.repository;

import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {
    @Query("""
                SELECT rs FROM ReservationSlot rs
                    WHERE rs.restaurant.id = :restaurantId
                    AND rs.date = :date
                    AND rs.count < rs.restaurant.maxCapacity
            """)
    List<ReservationSlot> findAvailableTimes(@Param("restaurantId") Long restaurantId,
                                             @Param("date") LocalDate date);

    Optional<ReservationSlot> findByRestaurantIdAndDateAndTime(Long restaurantId, LocalDate reservationDate,
                                                               LocalTime reservationTime);
}
