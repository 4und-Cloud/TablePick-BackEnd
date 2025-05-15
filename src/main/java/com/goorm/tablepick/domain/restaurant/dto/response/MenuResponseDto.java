package com.goorm.tablepick.domain.restaurant.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MenuResponseDto {
    @Schema(description = "메뉴 이름", example = "삼겹살")
    String name;
    @Schema(description = "메뉴 가격", example = "13000")
    BigDecimal price;
}
