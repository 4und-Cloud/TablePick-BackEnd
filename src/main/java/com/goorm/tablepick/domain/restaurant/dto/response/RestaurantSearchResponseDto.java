package com.goorm.tablepick.domain.restaurant.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantImage;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
@JsonIgnoreProperties({"reservationSlots", "boards", "restaurantImages", "restaurantOperatingHours"})
public class RestaurantSearchResponseDto {
    @Schema(description = "식당 아이디", example = "1")
    private Long id;
    @Schema(description = "식당 이름", example = "더미 식당")
    private String name;
    @Schema(description = "식당 주소", example = "서울특별시 강남구 강남대로 11")
    private String address;
    @Schema(description = "식당 타임당 수용 가능 팀 수", example = "3")
    private Long maxCapacity;
    @Schema(description = "식당 x좌표", example = "2345.235655")
    private BigDecimal xcoordinate;
    @Schema(description = "식당 y좌표", example = "4989.235454")
    private BigDecimal ycoordinate;
    @Schema(description = "식당 번호", example = "023456789")
    private String restaurantPhoneNumber;
    @Schema(description = "식당 카테고리", example = "한식")
    private RestaurantCategory restaurantCategory;
    @Schema(description = "메뉴 리스트", example = "삼겹살")
    private List<MenuResponseDto> menus;
    @Schema(description = "게시물 리스트")
    private List<Board> boards;
    @Schema(description = "예약 가능 슬롯", example = "3")
    private List<ReservationSlot> reservationSlots;
    @Schema(description = "식당 이미지", example = "url")
    private List<RestaurantImage> restaurantImages;
    @Schema(description = "식당 운영 시간", example = "12:00-24:00")
    private List<RestaurantOperatingHour> restaurantOperatingHours;

    public static RestaurantSearchResponseDto toDto(Restaurant restaurant) {
        return RestaurantSearchResponseDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .restaurantPhoneNumber(restaurant.getRestaurantPhoneNumber())
                .address(restaurant.getAddress())
                .xcoordinate(restaurant.getXcoordinate())
                .ycoordinate(restaurant.getYcoordinate())
                .maxCapacity(restaurant.getMaxCapacity())
                .reservationSlots(restaurant.getReservationSlots())
                .restaurantCategory(restaurant.getRestaurantCategory())
                .restaurantImages(restaurant.getRestaurantImages())
                .restaurantOperatingHours(restaurant.getRestaurantOperatingHours())
                .menus(restaurant.getMenus().stream()
                        .map(menu -> new MenuResponseDto(menu.getName(), menu.getPrice()))
                        .collect(Collectors.toList()))
                .build();
    }

}
