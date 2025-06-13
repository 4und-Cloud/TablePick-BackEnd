package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.CategoryResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantDetailResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantSearchResponseDto;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantErrorCode;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantCategoryRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
@Transactional(readOnly = true)
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final BoardTagRepository boardTagRepository;

    @Override
    public Page<RestaurantResponseDto> getAllRestaurants(Pageable pageable) {
        Page<Restaurant> restaurantPage = restaurantRepository.findPopularRestaurants(pageable);

        Page<RestaurantResponseDto> dtoPage = restaurantPage.map(restaurant -> {
            List<String> topTags = boardTagRepository.findTopTagsByRestaurantIdNative(restaurant.getId());
            return new RestaurantResponseDto(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getRestaurantCategory().getName(),
                    topTags,
                    restaurant.getAddress(),
                    restaurant.getRestaurantImages().isEmpty() ? null
                            : restaurant.getRestaurantImages().get(0).getImageUrl()
            );
        });

        return dtoPage;
    }

    @Override
    public PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page) {
        Pageable pageable = PageRequest.of(page, 6);
        Page<Restaurant> restaurantList = restaurantRepository.findAllOrderByBoardNum(pageable);
        return new PagedRestaurantResponseDto(restaurantList);
    }

    @Override
    public RestaurantDetailResponseDto getRestaurantDetail(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));

        List<String> topTags = boardTagRepository.findTopTagsByRestaurantIdNative(restaurant.getId());

        return RestaurantDetailResponseDto.fromEntity(restaurant, topTags);
    }

    @Override
    public List<CategoryResponseDto> getCategoryList() {
        List<RestaurantCategory> categoryList = restaurantCategoryRepository.findAll();
        return categoryList.stream()
                .map(CategoryResponseDto::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RestaurantSearchResponseDto> searchRestaurantsV1(RestaurantSearchRequestDto requestDto) {
        String keyword = requestDto.getKeyword();
        List<Long> tagIds = requestDto.getTagIds();

        if (keyword != null && keyword.length() > 20) {
            throw new RestaurantException(RestaurantErrorCode.TOO_LONG_KEYWORD);
        }

        if (tagIds != null && !tagIds.isEmpty() && tagIds.size() > 3) {
            throw new RestaurantException(RestaurantErrorCode.TOO_MANY_TAGS);
        }
        return restaurantRepository.searchRestaurantResult(keyword, tagIds,requestDto.getSort(), requestDto.getOnlyOperating());
    }
}
