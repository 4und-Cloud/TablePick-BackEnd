package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantTag;
import java.util.List;
import org.antlr.v4.runtime.atn.SemanticContext.AND;
import org.antlr.v4.runtime.atn.SemanticContext.OR;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query(""" 
            SELECT DISTINCT r
            FROM Restaurant r LEFT JOIN r.menus m
            WHERE r.name LIKE %:keyword%
            OR m.name LIKE %:keyword%
            OR r.address LIKE %:keyword%
            """)
    Page<Restaurant> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT r FROM Restaurant r
            LEFT JOIN r.restaurantTags rt
            WHERE rt.tag.id IN :tagIds
            GROUP BY r.id
            HAVING COUNT(DISTINCT rt.id) = :cnt
            ORDER BY COUNT(rt.id) DESC
    """)
    Page<Restaurant> findAllByTags(@Param("tagIds") List<Long> tagIds, @Param("cnt") int cnt, Pageable pageable);


    @Query("""
            SELECT r FROM Restaurant r
            LEFT JOIN ReservationSlot slot ON r.id = slot.restaurant.id
            LEFT JOIN Reservation res ON slot.id = res.reservationSlot.id
            WHERE r.restaurantCategory IS NOT NULL
              AND SIZE(r.restaurantImages) > 0
            GROUP BY r.id
            ORDER BY COUNT(r.id) DESC
            """)
    Page<Restaurant> findPopularRestaurants(Pageable pageable);

    @Query("SELECT r FROM Restaurant r ORDER BY SIZE(r.boards) DESC")
    Page<Restaurant> findAllOrderedByCreatedAt(Pageable pageable);

    @Query("""
            SELECT r FROM Restaurant r
            JOIN r.restaurantTags rt
            JOIN r.menus m
            WHERE rt.tag.id IN :tagIds
              AND (r.name LIKE %:keyword%
                        OR m.name LIKE %:keyword%
                   OR r.address LIKE %:keyword%)
            GROUP BY r.id
            HAVING COUNT(DISTINCT rt.tag.id) = :cnt
    """)
    Page<Restaurant> findAllByKeywordAndTags(String keyword, List<Long> tagIds, int cnt, Pageable pageable);

    @Query("SELECT r FROM Restaurant r ORDER BY r.name ASC")
    Page<Restaurant> findAllOrderByNameAsc(Pageable pageable);  // 가나다순 정렬

}
