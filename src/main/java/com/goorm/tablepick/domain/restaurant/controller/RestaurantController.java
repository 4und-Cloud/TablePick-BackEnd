package com.goorm.tablepick.domain.restaurant.controller;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantCategorySearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantKeywordSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @GetMapping("/all")
    @Operation(summary = "전체 식당 목록 조회", description = "전체 식당 목록을 조회합니다.")
    public Page<RestaurantResponseDto> getAllRestaurants(
            @RequestParam(defaultValue = "0") int page
    ) {
        Pageable pageable = PageRequest.of(page, 4);
        return restaurantService.getAllRestaurants(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "키워드 식당 검색", description = "키워드로 식당이름과 메뉴, 주소를 통해 식당을 검색합니다.")
    public PagedRestaurantResponseDto searchRestaurantsByKeyword(
            @RequestBody @Valid RestaurantKeywordSearchRequestDto requestDto) {
        return restaurantService.searchAllByKeyword(requestDto);
    }

    @GetMapping("/search/category")
    @Operation(summary = "카테고리 식당 검색", description = "카테고리로 식당을 검색합니다.")
    public PagedRestaurantResponseDto searchByCategory(
            @RequestBody @Valid RestaurantCategorySearchRequestDto requestDto) {
        return restaurantService.searchAllByCategory(requestDto);
    }

}