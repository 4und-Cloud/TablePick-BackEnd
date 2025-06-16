package com.goorm.tablepick.domain.restaurant.repository;

import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepositoryCustom {
    Page<RestaurantSearchResponseDto> searchRestaurantResult(
            String keyword, List<Long> tagIds, String sort, Boolean onlyOperating, Pageable pageable);
}
