package com.goorm.tablepick.domain.reservation.repository;

import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rs FROM ReservationSlot rs WHERE rs.restaurant.id = :restaurantId AND rs.date = :date AND rs.time = :time")
    Optional<ReservationSlot> findWithPessimisticLock(Long restaurantId, LocalDate date, LocalTime time);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rs FROM ReservationSlot rs WHERE rs.id = :slotId")
    Optional<ReservationSlot> findByIdWithPessimisticLock(Long slotId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT rs FROM ReservationSlot rs WHERE rs.id = :slotId")
    Optional<ReservationSlot> findByIdWithOptimisticLock(Long slotId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT rs FROM ReservationSlot rs WHERE rs.restaurant.id = :restaurantId AND rs.date = :date AND rs.time = :time")
    Optional<ReservationSlot> findWithOptimisticLock(Long restaurantId, LocalDate date, LocalTime time);

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

    boolean existsByRestaurantAndDateAndTime(Restaurant restaurant, LocalDate date, LocalTime time);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rs FROM ReservationSlot rs WHERE rs.restaurant.id = :restaurantId AND rs.date = :date AND rs.time = :time")
    Optional<ReservationSlot> findByRestaurantIdAndDateAndTimeWithPessimisticLock(
            @Param("restaurantId") Long restaurantId,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT rs FROM ReservationSlot rs WHERE rs.restaurant.id = :restaurantId AND rs.date = :date AND rs.time = :time")
    Optional<ReservationSlot> findByRestaurantIdAndDateAndTimeWithOptimisticLock(
            @Param("restaurantId") Long restaurantId,
            @Param("date") LocalDate date,
            @Param("time") LocalTime time
    );
}
