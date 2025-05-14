package com.goorm.tablepick.domain.restaurant.controller;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantCategorySearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantKeywordSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;

    @GetMapping
    public String getAll() {
        return "식당 목록";
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

    @GetMapping("/list")
    @Operation(summary = "식당 목록", description = "식당 목록을 리뷰 많은 순으로 반환합니다.")
    public PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page) {
        return restaurantService.getAllRestaurantsOrderedByBoardNum(page);
    }

}