package com.goorm.tablepick.domain.restaurant.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantKeywordSearchRequestDto {

    @Min(value = 2, message = "검색어는 최소 2글자 이상이어야 합니다")
    @NotBlank(message = "검색어를 입력해주세요")
    private String keyword;

    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다")
    private int page = 1;

}