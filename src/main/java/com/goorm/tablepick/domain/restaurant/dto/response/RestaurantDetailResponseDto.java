package com.goorm.tablepick.domain.restaurant.dto.response;

import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RestaurantDetailResponseDto {
    @Schema(description = "식당 아이디", example = "1")
    private final Long id;

    @Schema(description = "식당 이름", example = "더미 식당")
    private final String name;

    @Schema(description = "식당 주소", example = "서울특별시 강남구 강남대로 11")
    private final String address;

    @Schema(description = "식당 번호", example = "023456789")
    private final String restaurantPhoneNumber;

    @Schema(description = "식당 카테고리", example = "한식")
    private final RestaurantCategory restaurantCategory;

    @Schema(description = "식당 이미지", example = "url")
    private final RestaurantImageResponseDto restaurantImage;

    @Schema(description = "식당 운영 시간", example = "12:00-24:00")
    private final List<RestaurantOperatingHourResponseDto> restaurantOperatingHours;

    @Schema(description = "식당 태그", example = "역이랑 가까워요")
    private final List<String> restaurantTags;

    @Schema(description = "메뉴 리스트")
    private final List<MenuResponseDto> menus;

    public static RestaurantDetailResponseDto fromEntity(Restaurant restaurant) {
        return RestaurantDetailResponseDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .restaurantPhoneNumber(restaurant.getRestaurantPhoneNumber())
                .restaurantCategory(restaurant.getRestaurantCategory())
                .restaurantImage(
                        restaurant.getRestaurantImages() != null && !restaurant.getRestaurantImages().isEmpty()
                                ? RestaurantImageResponseDto.from(restaurant.getRestaurantImages().get(0))
                                : null
                )
                .restaurantOperatingHours(
                        restaurant.getRestaurantOperatingHours() != null
                                ? restaurant.getRestaurantOperatingHours().stream()
                                .map(RestaurantOperatingHourResponseDto::from)
                                .collect(Collectors.toList())
                                : Collections.emptyList()
                )
                .restaurantTags(restaurant.getRestaurantTags() != null
                        ? restaurant.getRestaurantTags().stream()
                        .map(tag -> tag.getTag().getName())
                        .collect(Collectors.toList())
                        : Collections.emptyList()
                )
                .menus(restaurant.getMenus() != null
                        ? restaurant.getMenus().stream()
                        .map(menu -> new MenuResponseDto(menu.getName(), menu.getPrice()))
                        .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

}
