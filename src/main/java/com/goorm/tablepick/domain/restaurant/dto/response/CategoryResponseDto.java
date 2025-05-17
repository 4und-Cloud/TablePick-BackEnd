package com.goorm.tablepick.domain.restaurant.dto.response;

import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryResponseDto {
    @Schema(description = "카테고리 ID", example = "1")
    private Long id;
    @Schema(description = "카테고리 이름", example = "한식")
    private String name;

    public static CategoryResponseDto toDto(RestaurantCategory category) {
        return CategoryResponseDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
