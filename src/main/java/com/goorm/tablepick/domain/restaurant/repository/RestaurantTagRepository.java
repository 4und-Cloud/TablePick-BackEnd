package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantTagRepository extends JpaRepository<RestaurantTag, Long> {
}
