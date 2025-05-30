package com.goorm.tablepick.domain.board.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardTag;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class MyBoardListResponseDto {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 내용", example = "엄청 맛있어요")
    private String content;

    @Schema(description = "생성일", example = "2025-06-06")
    private LocalDateTime createdAt;

    @Schema(description = "식당 아이디", example = "1")
    private Long restaurantId;

    @Schema(description = "식당 이름", example = "골목식당")
    private String restaurantName;

    @Schema(description = "작성자", example = "작성자")
    private String nickName;

    @Schema(description = "게시글 이미지", example = "url")
    private String boardImage;

    @Schema(description = "게시글 태그", example = "조용해요, 맛있어요")
    @JsonIgnore
    private List<String> boardTags;




    public static MyBoardListResponseDto toDto(Board board) {
        List<String> boardTags = board.getBoardTags().stream().map(BoardTag::getTagName).toList();

        return MyBoardListResponseDto.builder()
                .id(board.getId())
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .restaurantId(board.getReservation().getRestaurant().getId())
                .restaurantName(board.getReservation().getRestaurant().getName())
                .nickName(board.getMember().getNickname())
                .boardImage(board.getBoardImages() != null && !board.getBoardImages().isEmpty()
                        ? board.getBoardImages().get(0).getImageUrl() // 수정됨
                        : null)
                .boardTags(boardTags)
                .build();
    }
}
