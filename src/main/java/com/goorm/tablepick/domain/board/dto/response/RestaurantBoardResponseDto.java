package com.goorm.tablepick.domain.board.dto.response;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantBoardResponseDto {
    private Long boardId;
    private String imageUrl;

    public static RestaurantBoardResponseDto from(Board board) {
        String firstImageUrl = board.getBoardImages().stream()
                .map(BoardImage::getImageUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        return RestaurantBoardResponseDto.builder()
                .boardId(board.getId())
                .imageUrl(firstImageUrl)
                .build();
    }
}
