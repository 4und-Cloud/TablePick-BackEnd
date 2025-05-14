package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("SELECT DISTINCT r " +
            "FROM Restaurant r LEFT JOIN r.menus m "
            + "WHERE r.name LIKE %:keyword% " +
            "OR m.name LIKE %:keyword%")
    Page<Restaurant> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT r FROM Restaurant r JOIN FETCH r.restaurantCategory rc WHERE rc.id = :categoryId")
    Page<Restaurant> findAllByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("""
                SELECT r FROM Restaurant r
                LEFT JOIN ReservationSlot slot ON r.id = slot.restaurant.id
                LEFT JOIN Reservation res ON slot.id = res.reservationSlot.id
                WHERE r.restaurantCategory IS NOT NULL
                  AND SIZE(r.restaurantImages) > 0
                GROUP BY r.id
                ORDER BY COUNT(res.id) DESC
            """)
    Page<Restaurant> findPopularRestaurants(Pageable pageable);


    @Query("SELECT r FROM Restaurant r ORDER BY SIZE(r.boards) DESC")
    Page<Restaurant> findAllOrderedByCreatedAt(Pageable pageable);

}
