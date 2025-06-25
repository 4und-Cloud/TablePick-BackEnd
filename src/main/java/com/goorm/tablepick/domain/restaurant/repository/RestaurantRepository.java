package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponse;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, RestaurantRepositoryCustom {

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

    boolean existsByName(String newName);

    List<Restaurant> findByNameIgnoreCase(String newName);


    @Query(
            value = """
        SELECT
          r.id,
          r.name,
          r.address,
          rc.name                            AS restaurantCategory,
          ( SELECT ri.image_url
              FROM restaurant_image ri
             WHERE ri.restaurant_id = r.id
             ORDER BY ri.id
             LIMIT 1
          )                                   AS restaurantImage,
          ( SELECT JSON_ARRAYAGG(tg.name)
            FROM (
              SELECT DISTINCT tg2.name
                FROM board_tag bt2
                JOIN tag tg2
                  ON bt2.tag_id = tg2.id
               WHERE bt2.restaurant_id = r.id
            ) AS tg
          )                                   AS boardTags
        FROM restaurant AS r
        JOIN restaurant_category AS rc
          ON rc.id = r.restaurant_category_id
         AND (:categoryId IS NULL OR rc.id = :categoryId)
        LEFT JOIN board_tag AS bt
          ON bt.restaurant_id = r.id
         AND (:tagCount = 0 OR bt.tag_id IN (:tagIds))
        LEFT JOIN board AS b
          ON b.restaurant_id = r.id
        WHERE
          (:keyword IS NULL
           OR LOWER(r.name)    LIKE CONCAT('%',:keyword,'%')
           OR LOWER(r.address) LIKE CONCAT('%',:keyword,'%')
           OR EXISTS (
               SELECT 1
                 FROM menu m1
                WHERE m1.restaurant_id = r.id
                  AND LOWER(m1.name) LIKE CONCAT('%',:keyword,'%')
           )
          )
          AND (
            :onlyOperating IS NULL
            OR EXISTS (
                SELECT 1
                  FROM restaurant_operating_hour roh
                 WHERE roh.restaurant_id = r.id
                   AND roh.day_of_week = :today
                   AND roh.is_holiday = FALSE
                   AND CURTIME() BETWEEN roh.open_time AND roh.close_time
            )
          )
          AND (
            :radiusKm IS NULL OR :lat IS NULL OR :lng IS NULL
            OR ST_Distance_Sphere(
                 POINT(r.xcoordinate, r.ycoordinate),
                 POINT(:lng, :lat)
               ) <= (:radiusKm * 1000)
          )
          AND (
            :minPrice IS NULL
            OR (
              SELECT MIN(m2.price)
                FROM menu m2
               WHERE m2.restaurant_id = r.id
            ) > :minPrice
          )
          AND (
            :maxPrice IS NULL
            OR (
              SELECT MAX(m3.price)
                FROM menu m3
               WHERE m3.restaurant_id = r.id
            ) < :maxPrice
          )
        GROUP BY
          r.id, r.name, r.address, rc.name
        HAVING
          :tagCount = 0
          OR COUNT(DISTINCT bt.tag_id) = :tagCount
        ORDER BY
          CASE WHEN :sort = 'boardCount'
               THEN (SELECT COUNT(DISTINCT b2.id) FROM board b2 WHERE b2.restaurant_id = r.id)
          END DESC,
          CASE WHEN :sort = 'reservationCount'
               THEN (SELECT COUNT(DISTINCT rs.id)
                       FROM reservation rs
                      WHERE rs.restaurant_id = r.id)
          END DESC
        LIMIT :#{#pageable.pageSize}
        OFFSET :#{#pageable.offset}
      """,
            countQuery = """
        SELECT COUNT(DISTINCT r.id)
        FROM restaurant AS r
        JOIN restaurant_category AS rc
          ON rc.id = r.restaurant_category_id
         AND (:categoryId IS NULL OR rc.id = :categoryId)
        WHERE
          (:keyword IS NULL
           OR LOWER(r.name)    LIKE CONCAT('%',:keyword,'%')
           OR LOWER(r.address) LIKE CONCAT('%',:keyword,'%')
           OR EXISTS (
               SELECT 1
                 FROM menu m1
                WHERE m1.restaurant_id = r.id
                  AND LOWER(m1.name) LIKE CONCAT('%',:keyword,'%')
           )
          )
          AND (
            :onlyOperating IS NULL
            OR EXISTS (
                SELECT 1
                  FROM restaurant_operating_hour roh
                 WHERE roh.restaurant_id = r.id
                   AND roh.day_of_week = :today
                   AND roh.is_holiday = FALSE
                   AND CURTIME() BETWEEN roh.open_time AND roh.close_time
            )
          )
          AND (
            :radiusKm IS NULL OR :lat IS NULL OR :lng IS NULL
            OR ST_Distance_Sphere(
                 POINT(r.xcoordinate, r.ycoordinate),
                 POINT(:lng, :lat)
               ) <= (:radiusKm * 1000)
          )
          AND (
            :minPrice IS NULL
            OR (
              SELECT MIN(m2.price)
                FROM menu m2
               WHERE m2.restaurant_id = r.id
            ) > :minPrice
          )
          AND (
            :maxPrice IS NULL
            OR (
              SELECT MAX(m3.price)
                FROM menu m3
               WHERE m3.restaurant_id = r.id
            ) < :maxPrice
          )
      """,
            nativeQuery = true
    )
    Page<RestaurantSearchResponse> searchV0(
            @Param("keyword")       String keyword,
            @Param("tagIds")        List<Long> tagIds,
            @Param("tagCount")      long tagCount,
            @Param("sort")          String sort,
            @Param("onlyOperating") Boolean onlyOperating,
            @Param("today")         String today,
            @Param("radiusKm")      Integer radiusKm,
            @Param("lat")           Double lat,
            @Param("lng")           Double lng,
            @Param("categoryId")    Long categoryId,
            @Param("minPrice")      BigDecimal minPrice,
            @Param("maxPrice")      BigDecimal maxPrice,
            Pageable pageable
    );

    @Query(
            value = """
        WITH
          name_cands AS (
            SELECT id
            FROM restaurant
            WHERE :keyword IS NULL
              OR MATCH(name, address) AGAINST(:keyword IN BOOLEAN MODE)
          ),
          menu_cands AS (
            SELECT DISTINCT restaurant_id AS id
            FROM menu
            WHERE :keyword IS NULL
              OR MATCH(name) AGAINST(:keyword IN BOOLEAN MODE)
          ),
          all_cands AS (
            SELECT id FROM name_cands
            UNION
            SELECT id FROM menu_cands
          ),
          tag_counts AS (
            SELECT
              bt.restaurant_id,
              COUNT(DISTINCT bt.tag_id) AS matched_tag_count
            FROM board_tag bt
            WHERE bt.tag_id IN (:tagIds)
            GROUP BY bt.restaurant_id
          )
        SELECT
          r.id,
          r.name,
          r.address,
          rc.name AS restaurantCategory,
          (
            SELECT ri.image_url
            FROM restaurant_image ri
            WHERE ri.restaurant_id = r.id
            ORDER BY ri.id
            LIMIT 1
          ) AS restaurantImage,
          COALESCE(
            (
              SELECT JSON_ARRAYAGG(dt.tg_name)
              FROM (
                SELECT DISTINCT tg.name AS tg_name
                FROM board_tag bt2
                JOIN tag tg ON tg.id = bt2.tag_id
                WHERE bt2.restaurant_id = r.id
              ) dt
            ),
            JSON_ARRAY()
          ) AS boardTags
        FROM all_cands ac
        JOIN restaurant r ON r.id = ac.id
        JOIN restaurant_category rc ON rc.id = r.restaurant_category_id
        LEFT JOIN tag_counts tc ON tc.restaurant_id = r.id
        WHERE
          (:onlyOperating = FALSE
            OR EXISTS (
              SELECT 1
              FROM restaurant_operating_hour roh
              WHERE roh.restaurant_id = r.id
                AND roh.day_of_week = :today
                AND roh.is_holiday = FALSE
                AND CURTIME() BETWEEN roh.open_time AND roh.close_time
            )
          )
          AND (
            :radiusKm IS NULL
            OR ST_Distance_Sphere(
              POINT(r.xcoordinate, r.ycoordinate),
              POINT(:lng, :lat)
            ) <= (:radiusKm * 1000)
          )
          AND (
            :minPrice IS NULL
            OR (
              SELECT MIN(m2.price)
              FROM menu m2
              WHERE m2.restaurant_id = r.id
            ) >= :minPrice
          )
          AND (
            :maxPrice IS NULL
            OR (
              SELECT MAX(m3.price)
              FROM menu m3
              WHERE m3.restaurant_id = r.id
            ) <= :maxPrice
          )
          AND (
            :tagCount = 0
            OR COALESCE(tc.matched_tag_count, 0) = :tagCount
          )
        GROUP BY
          r.id, r.name, r.address, rc.name
        ORDER BY
          CASE WHEN :sort = 'boardCount' THEN (
            SELECT COUNT(*)
            FROM board b
            WHERE b.restaurant_id = r.id
          ) END DESC,
          CASE WHEN :sort = 'reservationCount' THEN (
            SELECT COUNT(*)
            FROM reservation rs
            WHERE rs.restaurant_id = r.id
          ) END DESC
        LIMIT :#{#pageable.pageSize}
        OFFSET :#{#pageable.offset};
      """,
            countQuery = """
        WITH
          name_cands AS (
            SELECT id
            FROM restaurant
            WHERE :keyword IS NULL
              OR MATCH(name, address) AGAINST(:keyword IN BOOLEAN MODE)
          ),
          menu_cands AS (
            SELECT DISTINCT restaurant_id AS id
            FROM menu
            WHERE :keyword IS NULL
              OR MATCH(name) AGAINST(:keyword IN BOOLEAN MODE)
          ),
          all_cands AS (
            SELECT id FROM name_cands
            UNION
            SELECT id FROM menu_cands
          ),
          tag_counts AS (
            SELECT
              bt.restaurant_id,
              COUNT(DISTINCT bt.tag_id) AS matched_tag_count
            FROM board_tag bt
            WHERE bt.tag_id IN (:tagIds)
            GROUP BY bt.restaurant_id
          )
        SELECT COUNT(*)
        FROM all_cands ac
        JOIN restaurant r ON r.id = ac.id
        JOIN restaurant_category rc ON rc.id = r.restaurant_category_id
        LEFT JOIN tag_counts tc ON tc.restaurant_id = r.id
        WHERE
          (:onlyOperating = FALSE
            OR EXISTS (
              SELECT 1
              FROM restaurant_operating_hour roh
              WHERE roh.restaurant_id = r.id
                AND roh.day_of_week = :today
                AND roh.is_holiday = FALSE
                AND CURTIME() BETWEEN roh.open_time AND roh.close_time
            )
          )
          AND (
            :radiusKm IS NULL
            OR ST_Distance_Sphere(
              POINT(r.xcoordinate, r.ycoordinate),
              POINT(:lng, :lat)
            ) <= (:radiusKm * 1000)
          )
          AND (
            :minPrice IS NULL
            OR (
              SELECT MIN(m2.price)
              FROM menu m2
              WHERE m2.restaurant_id = r.id
            ) >= :minPrice
          )
          AND (
            :maxPrice IS NULL
            OR (
              SELECT MAX(m3.price)
              FROM menu m3
              WHERE m3.restaurant_id = r.id
            ) <= :maxPrice
          )
          AND (
            :tagCount = 0
            OR COALESCE(tc.matched_tag_count, 0) = :tagCount
          )
      """,
            nativeQuery = true
    )
    Page<RestaurantSearchResponse> searchV2(
            @Param("keyword") String keyword,
            @Param("tagIds") List<Long> tagIds,
            @Param("tagCount") long tagCount,
            @Param("sort") String sort,
            @Param("onlyOperating") Boolean onlyOperating,
            @Param("today") String today,
            @Param("radiusKm") Integer radiusKm,
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

}
