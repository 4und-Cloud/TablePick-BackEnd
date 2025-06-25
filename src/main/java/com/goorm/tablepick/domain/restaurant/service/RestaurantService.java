package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.CategoryResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantDetailResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantService {

    Page<RestaurantResponseDto> getAllRestaurants(Pageable pageable, Member member);

    PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page, Member member);

    RestaurantDetailResponseDto getRestaurantDetail(Long id, Member member);

    List<CategoryResponseDto> getCategoryList();

    Page<RestaurantSearchResponseDto> searchRestaurantsV1(RestaurantSearchRequestDto requestDto);

    Page<RestaurantSearchResponseDto> searchRestaurantsV2(
            RestaurantSearchRequestDto requestDto
    );

}
