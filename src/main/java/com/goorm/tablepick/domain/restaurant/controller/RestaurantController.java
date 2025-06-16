package com.goorm.tablepick.domain.restaurant.controller;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantDetailResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import com.goorm.tablepick.domain.restaurant.service.RestaurantService;
import com.goorm.tablepick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = (userDetails != null) ? userDetails.getMember() : null;
        Pageable pageable = PageRequest.of(page, 4);
        return restaurantService.getAllRestaurants(pageable, member);
    }

    @GetMapping("/list")
    @Operation(summary = "식당 목록", description = "식당 목록을 리뷰 많은 순으로 반환합니다.")
    public PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(
            @RequestParam(defaultValue = "0") int page,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = (userDetails != null) ? userDetails.getMember() : null;
        return restaurantService.getAllRestaurantsOrderedByBoardNum(page, member);
    }

    @GetMapping("/{restaurantId}")
    @Operation(summary = "식당 상세 조회", description = "특정 식당의 상세 정보를 조회합니다.")
    public RestaurantDetailResponseDto getRestaurantDetail(
            @PathVariable @Parameter(description = "식당 ID", example = "1") Long restaurantId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Member member = (userDetails != null) ? userDetails.getMember() : null;

        return restaurantService.getRestaurantDetail(restaurantId, member);
    }

    @GetMapping("/v1/search")
    @Operation(summary = "식당 검색", description = "키워드와 태그로 식당이름과 주소, 메뉴 이름, 태그을 통해 식당을 검색합니다.")
    public Page<RestaurantSearchResponseDto> searchRestaurantsV1(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) boolean onlyOperating,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page) {
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword(keyword)
                .tagIds(tagIds)
                .onlyOperating(onlyOperating)
                .sort(sort)
                .page(page).build();
        return restaurantService.searchRestaurantsV1(requestDto);
    }
}