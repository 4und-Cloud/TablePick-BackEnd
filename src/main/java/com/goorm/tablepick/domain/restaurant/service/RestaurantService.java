package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.*;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestaurantService {
    PagedRestaurantResponseDto searchRestaurants(RestaurantSearchRequestDto keywordSearchRequestDto);

    Page<RestaurantResponseDto> getAllRestaurants(Pageable pageable, Member member);

    PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page, Member member);

    RestaurantDetailResponseDto getRestaurantDetail(Long id, Member member);

    List<CategoryResponseDto> getCategoryList();

    PagedRestaurantSearchResponseDto searchRestaurantsV1(RestaurantSearchRequestDto requestDto);
}
