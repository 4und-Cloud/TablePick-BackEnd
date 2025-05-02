package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantOperatingHourRepository extends JpaRepository<RestaurantOperatingHour, Long> {
}
