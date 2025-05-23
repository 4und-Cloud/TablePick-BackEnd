package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantCategorySearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.CategoryResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantDetailResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantService {
    PagedRestaurantResponseDto searchRestaurants(RestaurantSearchRequestDto keywordSearchRequestDto);

    Page<RestaurantResponseDto> getAllRestaurants(Pageable pageable);

    PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page);

    RestaurantDetailResponseDto getRestaurantDetail(Long id);

    List<CategoryResponseDto> getCategoryList();
}
