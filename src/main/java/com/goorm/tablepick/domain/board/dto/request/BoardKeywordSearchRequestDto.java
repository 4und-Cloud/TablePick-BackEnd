package com.goorm.tablepick.domain.board.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BoardKeywordSearchRequestDto {

    @Min(value = 2, message = "검색어는 최소 2글자 이상이어야 합니다")
    @NotBlank(message = "검색어를 입력해주세요")
    private String keyword;

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private int page = 0;
}
