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
}
