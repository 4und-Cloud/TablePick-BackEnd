package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantOperatingHourRepository extends JpaRepository<RestaurantOperatingHour, Long> {
    Optional<RestaurantOperatingHour> findByRestaurantId(Long restaurantId);
}
