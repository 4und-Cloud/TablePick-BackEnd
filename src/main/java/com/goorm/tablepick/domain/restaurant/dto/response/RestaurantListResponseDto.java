package com.goorm.tablepick.domain.restaurant.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantImage;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantOperatingHour;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantTag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties({"restaurantImages", "restaurantOperatingHours", "restaurantTags"})
public class RestaurantListResponseDto {
    @Schema(description = "식당 아이디", example = "1")
    private Long id;
    @Schema(description = "식당 이름", example = "더미 식당")
    private String name;
    @Schema(description = "식당 주소", example = "서울특별시 강남구 강남대로 11")
    private String address;
    @Schema(description = "식당 번호", example = "023456789")
    private String restaurantPhoneNumber;
    @Schema(description = "식당 카테고리", example = "한식")
    private RestaurantCategory restaurantCategory;
    @Schema(description = "식당 이미지", example = "url")
    private RestaurantImage restaurantImage;
    @Schema(description = "식당 운영 시간", example = "12:00-24:00")
    private List<RestaurantOperatingHour> restaurantOperatingHours;
    @Schema(description = "식당 태그", example = "역이랑 가까워요")
    private List<RestaurantTag> restaurantTags;

    public static RestaurantListResponseDto toDto(Restaurant restaurant) {
        return RestaurantListResponseDto.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .restaurantPhoneNumber(restaurant.getRestaurantPhoneNumber())
                .address(restaurant.getAddress())
                .restaurantCategory(restaurant.getRestaurantCategory())
                .restaurantImage(
                        restaurant.getRestaurantImages().get(0) == null ? restaurant.getRestaurantImages().get(0)
                                : null)
                .restaurantOperatingHours(restaurant.getRestaurantOperatingHours())
                .restaurantTags(restaurant.getRestaurantTags())
                .build();
    }

}
