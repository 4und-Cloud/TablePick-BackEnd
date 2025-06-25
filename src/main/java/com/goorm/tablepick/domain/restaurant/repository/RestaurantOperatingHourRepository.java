package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantOperatingHourRepository extends JpaRepository<RestaurantOperatingHour, Long> {

    List<RestaurantOperatingHour> findAllByRestaurantId(Long id);

    List<RestaurantOperatingHour> findByRestaurantId(java.lang.Long id);
}
