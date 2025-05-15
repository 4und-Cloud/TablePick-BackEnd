package com.goorm.tablepick.domain.restaurant.dto.response;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantTag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RestaurantTagResponseDto {
    @Schema(description = "태그 이름", example = "분위기가 좋아요")
    private final String tagName;

    public static RestaurantTagResponseDto from(RestaurantTag restaurantTag) {
        return RestaurantTagResponseDto.builder()
                .tagName(restaurantTag.getTag().getName())
                .build();
    }
}
