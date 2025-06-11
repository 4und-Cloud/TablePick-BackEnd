package com.goorm.tablepick.domain.restaurant.dto.response;

import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PagedRestaurantSearchResponseDto {
    private List<RestaurantSearchResponseDto> restaurants;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private long startNumber; // 첫 번호 (1-based)
    private long endNumber;   // 끝 번호


    public static PagedRestaurantSearchResponseDto create(Page<RestaurantSearchResponseDto> page) {
        return PagedRestaurantSearchResponseDto.builder()
                .restaurants(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .startNumber((long) page.getNumber() * page.getSize()+1)
                .endNumber((long) page.getNumber() * page.getSize()-1)
                .build();
    }

}
