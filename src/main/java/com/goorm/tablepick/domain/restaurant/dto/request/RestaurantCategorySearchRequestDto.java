package com.goorm.tablepick.domain.restaurant.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantCategorySearchRequestDto {

    @NotNull(message = "카테고리 ID를 입력해주세요")
    private Long categoryId;

    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private int page = 1;

}