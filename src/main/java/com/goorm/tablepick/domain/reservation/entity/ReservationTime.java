package com.goorm.tablepick.domain.reservation.entity;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_operating_hour")
    private RestaurantOperatingHour restaurantOperatingHour;

    private Long count;
}
