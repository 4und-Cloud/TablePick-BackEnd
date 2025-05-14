package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantCategorySearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantKeywordSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantService {
    PagedRestaurantResponseDto searchAllByKeyword(@Valid RestaurantKeywordSearchRequestDto keywordSearchRequestDto);

    PagedRestaurantResponseDto searchAllByCategory(@Valid RestaurantCategorySearchRequestDto categorySearchRequestDto);

    Page<RestaurantResponseDto> getAllRestaurants(Pageable pageable);
}
