package com.goorm.tablepick.domain.restaurant.entity;

import com.goorm.tablepick.domain.board.entity.BoardTag;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String restaurantPhoneNumber;

    private String address;

    private Double xcoordinate;

    private Double ycoordinate;

    private Long maxCapacity;

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<ReservationSlot> reservationSlots = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Menu> menus = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<RestaurantImage> restaurantImages = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<BoardTag> boardTags = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<RestaurantOperatingHour> restaurantOperatingHours = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "restaurant_category_id")
    private RestaurantCategory restaurantCategory;


}
