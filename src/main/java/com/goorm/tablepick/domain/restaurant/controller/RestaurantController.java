package com.goorm.tablepick.domain.restaurant.controller;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantDetailResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    @Operation(summary = "식당 검색", description = "키워드와 태그로 식당이름과 메뉴, 주소, 태그를 통해 식당을 검색합니다.")
    public PagedRestaurantResponseDto searchRestaurants(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(defaultValue = "1") int page) {
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword(keyword)
                .tagIds(tagIds)
                .page(page).build();
        return restaurantService.searchRestaurants(requestDto);
    }

    @GetMapping("/list")
    @Operation(summary = "식당 목록", description = "식당 목록을 리뷰 많은 순으로 반환합니다.")
    public PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page) {
        return restaurantService.getAllRestaurantsOrderedByBoardNum(page);
    }

    @GetMapping("/{restaurantId}")
    @Operation(summary = "식당 상세 조회", description = "특정 식당의 상세 정보를 조회합니다.")
    public RestaurantDetailResponseDto getRestaurantDetail(
            @PathVariable @Parameter(description = "식당 ID", example = "1") Long restaurantId) {
        return restaurantService.getRestaurantDetail(restaurantId);
    }
}