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
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Query(""" 
            SELECT DISTINCT r
            FROM Restaurant r LEFT JOIN r.menus m
            WHERE r.name LIKE %:keyword%
            OR m.name LIKE %:keyword%
            OR r.address LIKE %:keyword%
            """)
    Page<Restaurant> findAllByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
            SELECT
                r.id,
                r.address,
                r.max_capacity,
                r.name,
                r.restaurant_category_id,
                r.restaurant_phone_number,
                r.xcoordinate,
                r.ycoordinate
            FROM restaurant r
                JOIN board_tag bt ON bt.restaurant_id = r.id
            WHERE bt.tag_id IN (:tagIds)
            GROUP BY r.id
            HAVING COUNT(DISTINCT bt.tag_id) = :tagCount
            ORDER BY COUNT(DISTINCT bt.id) DESC
            """, nativeQuery = true)
    Page<Restaurant> findAllByTags(@Param("tagIds") List<Long> tagIds, @Param("tagCount") int tagCount,
                                   Pageable pageable);


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

    @Query(value = """
            SELECT
                r.id,
                r.address,
                r.max_capacity,
                r.name,
                r.restaurant_category_id,
                r.restaurant_phone_number,
                r.xcoordinate,
                r.ycoordinate
            FROM restaurant r
                JOIN board_tag bt ON bt.restaurant_id = r.id
                JOIN menu m ON r.id = m.restaurant_id
            WHERE bt.tag_id IN (:tagIds)
              AND (r.name LIKE %:keyword% OR m.name LIKE %:keyword% OR r.address LIKE %:keyword%)
            GROUP BY r.id
            HAVING COUNT(DISTINCT bt.tag_id) = :tagCount
            ORDER BY COUNT(DISTINCT bt.id) DESC
            """,
            nativeQuery = true)
    Page<Restaurant> findAllByKeywordAndTags(
            @Param("keyword") String keyword,
            @Param("tagIds") List<Long> tagIds,
            @Param("tagCount") int tagCount,
            Pageable pageable);

    @Query("SELECT r FROM Restaurant r ORDER BY r.name ASC")
    Page<Restaurant> findAllOrderByNameAsc(Pageable pageable);  // 가나다순 정렬


}
