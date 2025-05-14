package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantCategorySearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantKeywordSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantErrorCode;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantCategoryRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;

    @Override
    public PagedRestaurantResponseDto searchAllByKeyword(
            @Valid RestaurantKeywordSearchRequestDto keywordSearchRequestDto) {
        Pageable pageable = PageRequest.of(keywordSearchRequestDto.getPage() - 1, 6, Sort.by("name").ascending());
        Page<Restaurant> restaurantListByKeyword = restaurantRepository.findAllByKeyword(
                keywordSearchRequestDto.getKeyword(), pageable);

        return new PagedRestaurantResponseDto(restaurantListByKeyword);
    }

    @Override
    public PagedRestaurantResponseDto searchAllByCategory(
            @Valid RestaurantCategorySearchRequestDto categorySearchRequestDto) {
        Pageable pageable = PageRequest.of(categorySearchRequestDto.getPage() - 1, 6, Sort.by("name").ascending());
        Long categoryId = categorySearchRequestDto.getCategoryId();
        if (!restaurantCategoryRepository.existsById(categoryId)) {
            throw new RestaurantException(RestaurantErrorCode.NO_RESTAURANT_CATEGORY);
        }
        Page<Restaurant> restaurantListByCategory = restaurantRepository.findAllByCategory(categoryId,
                pageable);
        return new PagedRestaurantResponseDto(restaurantListByCategory);
    }

    @Override
    public PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page) {
        Pageable pageable = PageRequest.of(page - 1, 6);
        Page<Restaurant> restaurantList = restaurantRepository.findAllOrderedByCreatedAt(pageable);
        return new PagedRestaurantResponseDto(restaurantList);
    }
}
