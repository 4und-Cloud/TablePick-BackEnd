package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import java.util.List;

public interface RestaurantRepositoryCustom {
    List<RestaurantSearchResponseDto> searchRestaurantResult(
            String keyword, List<Long> tagIds, String sort, Boolean onlyOperating);
}
