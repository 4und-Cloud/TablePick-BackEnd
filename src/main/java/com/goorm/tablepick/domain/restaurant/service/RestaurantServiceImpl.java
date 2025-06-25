package com.goorm.tablepick.domain.restaurant.service;

import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
import com.goorm.tablepick.domain.member.entity.Member;
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
import com.goorm.tablepick.domain.userevent.dto.UserActionEventDto;
import com.goorm.tablepick.domain.userevent.service.UserEventService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
@Primary
@Transactional(readOnly = true)
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantCategoryRepository restaurantCategoryRepository;
    private final BoardTagRepository boardTagRepository;
    private final UserEventService userEventService;
    private final RestTemplate restTemplate;

    @Value("${ai.host}")
    private String aiHostUrl;

    @Override
    public Page<RestaurantResponseDto> getAllRestaurants(Pageable pageable, Member member) {
        Page<Restaurant> restaurantPage;

        if (member != null) {
            long userId = member.getId();

            // 1. AI 서버에서 추천 식당 ID 리스트 받아오기
            List<Long> recommendedRestaurantIds = getRecommendedRestaurantIds(userId);

            if (recommendedRestaurantIds.isEmpty()) {
                // 추천 결과가 없을 경우 fallback
                restaurantPage = restaurantRepository.findPopularRestaurants(pageable);
            } else {
                // 2. 추천 ID 기반 식당 조회
                restaurantPage = restaurantRepository.findRestaurantsByIdsInOrder(recommendedRestaurantIds, pageable);
            }
        } else {
            // 3. 비회원 fallback
            restaurantPage = restaurantRepository.findPopularRestaurants(pageable);
        }

        return restaurantPage.map(restaurant -> {
            List<String> topTags = boardTagRepository.findTopTagsByRestaurantIdNative(restaurant.getId());

            if (member != null) {
                UserActionEventDto event = new UserActionEventDto(
                        "RESTAURANT_VIEW",
                        restaurant.getId(),
                        member.getId(),
                        System.currentTimeMillis()
                );
                userEventService.sendClickEvent(event);
            }

            return new RestaurantResponseDto(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getRestaurantCategory().getName(),
                    topTags,
                    restaurant.getAddress(),
                    restaurant.getRestaurantImages().isEmpty() ? null
                            : restaurant.getRestaurantImages().getFirst().getImageUrl()
            );
        });
    }


    @Override
    public PagedRestaurantResponseDto getAllRestaurantsOrderedByBoardNum(int page, Member member) {
        Pageable pageable = PageRequest.of(page, 30);
        Page<Restaurant> restaurantList;

        if (member != null) {
            long userId = member.getId();

            // 1. AI 서버에서 추천 식당 ID 가져오기
            List<Long> recommendedRestaurantIds = getRecommendedRestaurantIds(userId);

            if (recommendedRestaurantIds.isEmpty()) {
                // 비회원과 동일하게 fallback 처리
                restaurantList = restaurantRepository.findAllOrderByBoardNum(pageable);
            } else {
                // 2. 추천 식당 정보 조회 (순서를 유지하는 쿼리 필요)
                restaurantList = restaurantRepository.findRestaurantsByIdsInOrder(recommendedRestaurantIds, pageable);
            }

            // 3. 로그 전송
            restaurantList.forEach(restaurant -> {
                UserActionEventDto event = new UserActionEventDto(
                        "RESTAURANT_VIEW",
                        restaurant.getId(),
                        userId,
                        System.currentTimeMillis()
                );
                userEventService.sendClickEvent(event);
            });

        } else {
            // 비회원 fallback 처리
            restaurantList = restaurantRepository.findAllOrderByBoardNum(pageable);
        }

        return new PagedRestaurantResponseDto(restaurantList);
    }


    @Override
    public RestaurantDetailResponseDto getRestaurantDetail(Long id, Member member) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantException(RestaurantErrorCode.NOT_FOUND));
        
        List<String> topTags = boardTagRepository.findTopTagsByRestaurantIdNative(restaurant.getId());
        
        if (member != null) {
            UserActionEventDto event = new UserActionEventDto(
                    "RESTAURANT_CLICK",
                    id,
                    member.getId(),
                    System.currentTimeMillis()
            );
            userEventService.sendClickEvent(event);
        }
        
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
    public Page<RestaurantSearchResponseDto> searchRestaurantsV1(RestaurantSearchRequestDto requestDto) {
        String keyword = requestDto.getKeyword();
        List<Long> tagIds = requestDto.getTagIds();
        Pageable pageable = PageRequest.of(requestDto.getPage(), 6);
        if (keyword != null && keyword.length() > 20) {
            throw new RestaurantException(RestaurantErrorCode.TOO_LONG_KEYWORD);
        }
        
        if (tagIds != null && !tagIds.isEmpty() && tagIds.size() > 3) {
            throw new RestaurantException(RestaurantErrorCode.TOO_MANY_TAGS);
        }
        return restaurantRepository.searchRestaurantResult(keyword, tagIds, requestDto.getSort(),
                requestDto.getOnlyOperating(), pageable);
    }

    public List<Long> getRecommendedRestaurantIds(Long userId) {
        String url = String.format(aiHostUrl + "/recommend/restaurants/%d", userId);
        try {
            ResponseEntity<List<Long>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            List<Long> restaurantIds = response.getBody();
            return (restaurantIds != null) ? restaurantIds : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            log.error("AI 서버 요청 실패: userId={}, status={}, response={}", userId, e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("AI 서버에서 추천 식당 ID를 가져오지 못했습니다. userId={}", userId, e);
            return Collections.emptyList();
        }
    }
}
