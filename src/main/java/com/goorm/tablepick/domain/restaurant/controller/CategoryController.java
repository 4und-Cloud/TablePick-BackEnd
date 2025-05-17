package com.goorm.tablepick.domain.restaurant.controller;

import com.goorm.tablepick.domain.restaurant.dto.response.CategoryResponseDto;
import com.goorm.tablepick.domain.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final RestaurantService restaurantService;

    @GetMapping
    @Operation(summary = "카테고리 조회", description = "전체 식당 카테고리을 조회합니다.")
    public ResponseEntity<List<CategoryResponseDto>> getCategoryList() {
        return ResponseEntity.ok(restaurantService.getCategoryList());
    }
}
