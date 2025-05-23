package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.CategoryResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantDetailResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantErrorCode;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantCategoryRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantTagRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final RestaurantTagRepository restaurantTagRepository;

    @Override
    public PagedRestaurantResponseDto searchRestaurants(@Valid RestaurantSearchRequestDto dto) {
        Pageable pageable = PageRequest.of(dto.getPage() - 1, 6, Sort.by("name").ascending());

        String keyword = dto.getKeyword();
        List<Long> tagIds = dto.getTagIds();

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasTags = tagIds != null && !tagIds.isEmpty();

        Page<Restaurant> restaurantList;
        //키워드, 태그 검색
        if (hasKeyword && hasTags) {
            restaurantList = restaurantRepository.findAllByKeywordAndTags(
                    keyword, tagIds, tagIds.size(), pageable);
            log.info("둘다 검색 -> "+keyword + tagIds);
            return new PagedRestaurantResponseDto(restaurantList);
        }
        //키워드 검색
        if (hasKeyword) {
            restaurantList = restaurantRepository.findAllByKeyword(keyword, pageable);
            log.info("키워드로만 검색 -> " + keyword+ tagIds);
            return new PagedRestaurantResponseDto(restaurantList);
        }
        //태그 검색
        if (hasTags) {
            restaurantList = restaurantRepository.findAllByTags(tagIds, tagIds.size(), pageable);
            log.info("태그로만 검색 -> "+keyword+ tagIds);
            return new PagedRestaurantResponseDto(restaurantList);
        }
        //키워드, 태그 둘 다 없으면 인기순으로 식당 목록 조회
        restaurantList = restaurantRepository.findPopularRestaurants(pageable);
        return new PagedRestaurantResponseDto(restaurantList);
    }


    @Override
    public Page<RestaurantResponseDto> getAllRestaurants(Pageable pageable) {
        Page<Restaurant> restaurantPage = restaurantRepository.findPopularRestaurants(pageable);
        Page<RestaurantResponseDto> dtoPage = restaurantPage.map(restaurant ->
                new RestaurantResponseDto(
                        restaurant.getId(),
                        restaurant.getName(),
                        restaurant.getRestaurantCategory().getName(),
                        restaurant.getAddress(),
                        restaurant.getRestaurantImages().isEmpty() ? null
                                : restaurant.getRestaurantImages().get(0).getImageUrl(),
                        restaurant.getRestaurantTags() != null
                                ? restaurant.getRestaurantTags().stream()
                                .map(tag -> tag.getTag().getName())
                                .collect(Collectors.toList())
                                : Collections.emptyList()
                )
        );
        return dtoPage;
    }

    @Override
    public PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page) {
        Pageable pageable = PageRequest.of(page - 1, 6);
        Page<Restaurant> restaurantList = restaurantRepository.findAllOrderByNameAsc(pageable);
        return new PagedRestaurantResponseDto(restaurantList);
    }

    @Override
    public RestaurantDetailResponseDto getRestaurantDetail(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));
        return RestaurantDetailResponseDto.fromEntity(restaurant);
    }

    @Override
    public List<CategoryResponseDto> getCategoryList() {
        List<RestaurantCategory> categoryList = restaurantCategoryRepository.findAll();
        return categoryList.stream()
                .map(CategoryResponseDto::toDto)
                .collect(Collectors.toList());
    }
}
