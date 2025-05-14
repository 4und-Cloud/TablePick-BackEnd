package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantCategorySearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantKeywordSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import jakarta.validation.Valid;

public interface RestaurantService {
    PagedRestaurantResponseDto searchAllByKeyword(@Valid RestaurantKeywordSearchRequestDto keywordSearchRequestDto);

    PagedRestaurantResponseDto searchAllByCategory(@Valid RestaurantCategorySearchRequestDto categorySearchRequestDto);

    PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page);
}
