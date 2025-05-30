package com.goorm.tablepick.domain.restaurant.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RestaurantSearchRequestDto {

    private String keyword;

    private List<Long> tagIds;

    private int page;

}