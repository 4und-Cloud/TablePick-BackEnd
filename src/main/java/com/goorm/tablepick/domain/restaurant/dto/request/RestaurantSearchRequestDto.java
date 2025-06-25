package com.goorm.tablepick.domain.restaurant.dto.request;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RestaurantSearchRequestDto {

    private String keyword;
    private List<Long> tagIds;
    private Boolean onlyOperating;
    private String sort;
    private int page;
    private Integer radiusKm;
    private Double lat;
    private Double lng;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}