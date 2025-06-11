package com.goorm.tablepick.domain.restaurant.dto.response;

import com.goorm.tablepick.domain.board.entity.BoardTag;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantImage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantSearchResponseDto {

    @Schema(description = "식당 아이디", example = "1")
    private Long id;

    @Schema(description = "식당 이름", example = "더미 식당")
    private String name;

    @Schema(description = "식당 주소", example = "서울특별시 강남구 강남대로 11")
    private String address;

    @Schema(description = "식당 카테고리", example = "한식")
    private String restaurantCategory;

    @Schema(description = "식당 이미지", example = "url")
    private String restaurantImage;

    @Schema(description = "식당 태그", example = "역이랑 가까워요")
    private List<String> boardTags;


}
