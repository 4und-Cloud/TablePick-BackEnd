package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.*;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantErrorCode;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantCategoryRepository;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final BoardTagRepository boardTagRepository;

    @Override
    public PagedRestaurantResponseDto searchRestaurants(@Valid RestaurantSearchRequestDto dto) {
        Pageable pageable = PageRequest.of(dto.getPage(), 6);

        String keyword = dto.getKeyword();
        List<Long> tagIds = dto.getTagIds();

        boolean hasKeyword = keyword != null && !keyword.isEmpty();
        boolean hasTags = tagIds != null && !tagIds.isEmpty();
        if (keyword != null && keyword.length() > 20) {
            throw new RestaurantException(RestaurantErrorCode.TOO_LONG_KEYWORD);
        }

        if (tagIds != null && !tagIds.isEmpty() && tagIds.size() > 3) {
            throw new RestaurantException(RestaurantErrorCode.TOO_MANY_TAGS);
        }
        Page<Restaurant> restaurantList;
        //키워드, 태그 검색
        if (hasKeyword && hasTags) {
            restaurantList = restaurantRepository.findAllByKeywordAndTags(
                    keyword, tagIds, tagIds.size(), pageable);
            log.info("둘다 검색 -> " + keyword + tagIds);
            if (restaurantList == null || restaurantList.isEmpty()) {
                return new PagedRestaurantResponseDto(Page.empty(pageable));
            }

            return new PagedRestaurantResponseDto(restaurantList);
        }
        //키워드 검색
        if (hasKeyword) {
            restaurantList = restaurantRepository.findAllByKeyword(keyword, pageable);
            log.info("키워드로만 검색 -> " + keyword + tagIds);
            if (restaurantList == null || restaurantList.isEmpty()) {
                return new PagedRestaurantResponseDto(Page.empty(pageable));
            }
            return new PagedRestaurantResponseDto(restaurantList);
        }
        //태그 검색
        if (hasTags) {
            restaurantList = restaurantRepository.findAllByTags(tagIds, tagIds.size(), pageable);
            log.info("태그로만 검색 -> " + keyword + tagIds);
            if (restaurantList == null || restaurantList.isEmpty()) {
                return new PagedRestaurantResponseDto(Page.empty(pageable));
            }
            return new PagedRestaurantResponseDto(restaurantList);
        }
        //키워드, 태그 둘 다 없으면 인기순으로 식당 목록 조회
        restaurantList = restaurantRepository.findPopularRestaurants(pageable);
        return new PagedRestaurantResponseDto(restaurantList);
    }


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
        Page<Restaurant> restaurantList = restaurantRepository.findAllOrderByNameAsc(pageable);
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
    public PagedRestaurantSearchResponseDto searchRestaurantsV1(RestaurantSearchRequestDto requestDto) {
        Pageable pageable = PageRequest.of(requestDto.getPage(), 6);
        String keyword = requestDto.getKeyword();
        List<Long> tagIds = requestDto.getTagIds();

        if (keyword != null && keyword.length() > 20) {
            throw new RestaurantException(RestaurantErrorCode.TOO_LONG_KEYWORD);
        }

        if (tagIds != null && !tagIds.isEmpty() && tagIds.size() > 3) {
            throw new RestaurantException(RestaurantErrorCode.TOO_MANY_TAGS);
        }
        PagedRestaurantSearchResponseDto pagedRestaurantSearchResponseDto = PagedRestaurantSearchResponseDto.create(restaurantRepository.searchRestaurantResult(keyword,
                tagIds, pageable));
        return pagedRestaurantSearchResponseDto;
    }
}
