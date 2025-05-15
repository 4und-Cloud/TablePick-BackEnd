package com.goorm.tablepick.domain.restaurant.dto.response;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class RestaurantImageResponseDto {
    @Schema(description = "식당 이미지 url", example = "http:어쩌구")
    private final String imageUrl;

    public static RestaurantImageResponseDto from(RestaurantImage image) {
        return RestaurantImageResponseDto.builder()
                .imageUrl(image.getImageUrl())
                .build();
    }
}

