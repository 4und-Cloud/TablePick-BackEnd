package com.goorm.tablepick.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.goorm.tablepick.domain.restaurant.dto.request.RestaurantSearchRequestDto;
import com.goorm.tablepick.domain.restaurant.dto.response.PagedRestaurantResponseDto;
import com.goorm.tablepick.domain.restaurant.dto.response.RestaurantListResponseDto;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantErrorCode;
import com.goorm.tablepick.domain.restaurant.exception.RestaurantException;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import com.goorm.tablepick.domain.restaurant.service.RestaurantService;
import com.goorm.tablepick.domain.restaurant.service.RestaurantServiceImpl;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;



@ExtendWith(MockitoExtension.class)
public class RestaurantServiceTest {

    @Mock
    private RestaurantService restaurantService;

    @Test
    @DisplayName("20자 이내의 키워드로 검색한다.")
    void searchByKeyword() {
        // given
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword("식당")
                .build();

        // Mock 응답 설정
        List<RestaurantListResponseDto> mockRestaurants = List.of(
                RestaurantListResponseDto.builder().name("골목 식당").build(),
                RestaurantListResponseDto.builder().name("바다 식당").build()
        );

        PagedRestaurantResponseDto mockResponse = PagedRestaurantResponseDto.builder()
                .restaurants(mockRestaurants)
                .build();

        // Mock 동작 정의
        when(restaurantService.searchRestaurants(any(RestaurantSearchRequestDto.class)))
                .thenReturn(mockResponse);

        // when
        PagedRestaurantResponseDto result = restaurantService.searchRestaurants(requestDto);

        // then
        Assertions.assertThat(result.getRestaurants()).hasSize(2)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 식당", "바다 식당");
    }

    @Test
    @DisplayName("21자 이상의 키워드로 검색하면 예외가 발생한다")
    void searchByKeywordOver20() {
        // given
        String invalidKeyword = "이것은_21자_이상의_너무_긴_검색어입니다";
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .keyword(invalidKeyword)
                .build();

        // when & then
        when(restaurantService.searchRestaurants(requestDto))
                .thenThrow(new RestaurantException(RestaurantErrorCode.TOO_LONG_KEYWORD));

        // when & then
        assertThatThrownBy(() -> restaurantService.searchRestaurants(requestDto))
                .isInstanceOf(RestaurantException.class)
                .hasMessageContaining("키워드는 20자 이내만 가능합니다.");
    }

    @Test
    @DisplayName("3개 이내의 키워드를 선택해 검색한다.")
    void searchByTag() {
        // given
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .tagIds(List.of(1L,2L,3L))
                .build();

        // Mock 응답 설정
        List<RestaurantListResponseDto> mockRestaurants = List.of(
                RestaurantListResponseDto.builder().name("골목 식당").build()
        );

        PagedRestaurantResponseDto mockResponse = PagedRestaurantResponseDto.builder()
                .restaurants(mockRestaurants)
                .build();

        // Mock 동작 정의
        when(restaurantService.searchRestaurants(any(RestaurantSearchRequestDto.class)))
                .thenReturn(mockResponse);

        // when
        PagedRestaurantResponseDto result = restaurantService.searchRestaurants(requestDto);

        // then
        Assertions.assertThat(result.getRestaurants()).hasSize(1)
                .extracting("name")
                .containsExactlyInAnyOrder("골목 식당");
    }

    @Test
    @DisplayName("4개 이상의 키워드를 선택해 검색한다.")
    void searchByTagOver3() {
        // given
        RestaurantSearchRequestDto requestDto = RestaurantSearchRequestDto.builder()
                .tagIds(List.of(1L,2L,3L,4L))
                .build();

        when(restaurantService.searchRestaurants(requestDto))
                .thenThrow(new RestaurantException(RestaurantErrorCode.TOO_MANY_TAGS));

        // when & then
        assertThatThrownBy(() -> restaurantService.searchRestaurants(requestDto))
                .isInstanceOf(RestaurantException.class)
                .hasMessageContaining("태그는 최대 3개까지 선택 가능합니다.");
    }


}
