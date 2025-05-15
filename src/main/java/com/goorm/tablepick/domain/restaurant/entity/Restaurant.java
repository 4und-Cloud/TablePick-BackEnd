package com.goorm.tablepick.domain.restaurant.entity;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String restaurantPhoneNumber;

    private String address;

    private BigDecimal xcoordinate;

    private BigDecimal ycoordinate;

    private Long maxCapacity;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<ReservationSlot> reservationSlots = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Menu> menus = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<RestaurantImage> restaurantImages = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<RestaurantTag> restaurantTags = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<RestaurantOperatingHour> restaurantOperatingHours = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "restaurant_category_id")
    private RestaurantCategory restaurantCategory;
}
