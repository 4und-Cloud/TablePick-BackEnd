package com.goorm.tablepick.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardCategorySearchRequestDto {
    @Schema(description = "카테고리 아이디", example = "1")
    @NotNull(message = "카테고리를 선택하여야 합니다.")
    Long categoryId;

    @Schema(description = "페이지 번호", example = "0")
    @Min(value = 0, message = "페이지는 0 이상이어야 합니다.")
    int page;

}
