package com.goorm.tablepick.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor // Jackson이 JSON으로 역직렬화할 때 필요
@AllArgsConstructor // @Builder와 함께 쓰면 좋음
public class BoardListResponseDto {
    private Long boardId;
    private String content;
    private String restaurantName;
    private String restaurantAddress;

    @ArraySchema(schema = @Schema(type = "string"))
    private List<String> tagNames;

    private String imageUrl;
}