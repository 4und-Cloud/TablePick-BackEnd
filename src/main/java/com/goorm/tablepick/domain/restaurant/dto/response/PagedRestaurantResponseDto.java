package com.goorm.tablepick.domain.restaurant.dto.response;

import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class PagedRestaurantResponseDto {
    private List<RestaurantListResponseDto> restaurants;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private long startNumber; // 첫 번호 (1-based)
    private long endNumber;   // 끝 번호

    public PagedRestaurantResponseDto(Page<Restaurant> page) {
        this.restaurants = page.getContent().stream()
                .map(RestaurantListResponseDto::toDto)
                .collect(Collectors.toList());
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.startNumber = (long) pageNumber * pageSize + 1;
        this.endNumber = startNumber + page.getNumberOfElements() - 1;
    }

}
