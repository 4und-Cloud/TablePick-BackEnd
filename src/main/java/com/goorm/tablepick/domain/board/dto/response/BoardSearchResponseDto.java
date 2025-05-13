package com.goorm.tablepick.domain.board.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardImage;
import com.goorm.tablepick.domain.board.entity.BoardTag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class BoardSearchResponseDto {
    @Schema(description = "게시물 아이디", example = "1")
    private Long id;
    @Schema(description = "게시물 내용", example = "처음가봤는데 아주 맛있었어요")
    private String content;
    @Schema(description = "생성일", example = "2025-05-05")
    private LocalDateTime createdAt;
    @Schema(description = "식당 이름", example = "더미 식당")
    private String restaurantName;
    @JsonIgnore
    @Schema(description = "게시글 썸네일 이미지")
    private BoardImage boardThumbImages;
    @JsonIgnore
    @Schema(description = "식당 태그", example = "분위기가 좋아요")
    private List<BoardTag> boardTags;

    public static BoardSearchResponseDto toDto(Board board) {
        return BoardSearchResponseDto.builder()
                .id(board.getId())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .restaurantName(board.getRestaurant().getName())
                .boardThumbImages(!board.getBoardImages().isEmpty() ? board.getBoardImages().get(0) : null)
                .boardTags(board.getBoardTags())
                .build();
    }
}
