package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> , RestaurantRepositoryCustom {

    @Query("""
            SELECT r FROM Restaurant r
            LEFT JOIN ReservationSlot slot ON r.id = slot.restaurant.id
            LEFT JOIN Reservation res ON slot.id = res.reservationSlot.id
            WHERE r.restaurantCategory IS NOT NULL
              AND SIZE(r.restaurantImages) > 0
            GROUP BY r.id
            ORDER BY COUNT(r.id) DESC, r.id
            """)
    Page<Restaurant> findPopularRestaurants(Pageable pageable);

    @Query(value = """
            SELECT r FROM Restaurant r
            LEFT JOIN Board b ON r.id = b.restaurantId
            GROUP BY r.id
            ORDER BY COUNT(r.id) DESC, r.id
            """)
    Page<Restaurant> findAllOrderByBoardNum(Pageable pageable);  // 가나다순 정렬

    // RestaurantRepository.java
    @Query("SELECT r FROM Restaurant r WHERE r.id IN :ids ORDER BY FIELD(r.id, :ids)")
    Page<Restaurant> findRestaurantsByIdsInOrder(@Param("ids") List<Long> ids, Pageable pageable);

}
