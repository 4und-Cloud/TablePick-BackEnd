package com.goorm.tablepick.domain.board.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardTag;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MyBoardListResponseDto {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;

    @Schema(description = "게시글 내용", example = "엄청 맛있어요")
    private String content;

    @Schema(description = "생성일", example = "2025-06-06")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-06-06")
    private LocalDateTime updatedAt;

    @Schema(description = "식당", example = "식당")
    @JsonIgnore
    private Restaurant restaurant;

    @Schema(description = "작성자", example = "작성자")
    private String nickName;

    @Schema(description = "게시글 이미지", example = "url")
    private String boardImage;

    @Schema(description = "게시글 태그", example = "조용해요, 맛있어요")
    @JsonIgnore
    private List<BoardTag> boardTags = new ArrayList<>();

    @Builder
    public MyBoardListResponseDto(Board board) {
        this.id = board.getId();
        this.content = board.getContent();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
        this.restaurant = board.getRestaurant();
        this.nickName = board.getMember().getNickname();
        this.boardImage =
                board.getBoardImages().get(0).getImageUrl() == null ? board.getBoardImages().get(0).getImageUrl()
                        : null;
        this.boardTags = board.getBoardTags();
    }
}
