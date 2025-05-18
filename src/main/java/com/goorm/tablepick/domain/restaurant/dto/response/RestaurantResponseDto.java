package com.goorm.tablepick.domain.restaurant.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RestaurantResponseDto {
    @Schema(description = "식당 ID", example = "3")
    private Long id;

    @Schema(description = "식당 이름", example = "엽기 떡볶이")
    private String name;

    @Schema(description = "식당 카테고리 이름", example = "한식")
    private String categoryName;

    @Schema(description = "식당 주소", example = "서울특별시 강남구 강남대로 11")
    private String address;

    @Schema(description = "이미지 url", example = "https://lh3.googleusercontent.com")
    private String imageUrl;

    @Schema(description = "식당 태그", example = "역이랑 가까워요")
    private final List<String> restaurantTags;


}
