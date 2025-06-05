package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantImageRepository extends JpaRepository<RestaurantImage, Long> {
}
