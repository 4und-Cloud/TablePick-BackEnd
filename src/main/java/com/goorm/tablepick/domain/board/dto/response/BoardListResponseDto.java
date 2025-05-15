package com.goorm.tablepick.domain.board.dto.response;

import com.goorm.tablepick.domain.board.entity.Board;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor // Jackson이 JSON으로 역직렬화할 때 필요
@AllArgsConstructor // @Builder와 함께 쓰면 좋음
public class BoardListResponseDto {
    private Long id;
    private String content;
    private String restaurantName;
    private String restaurantAddress;
    private String title;     // 혹시 추후 필요하면 사용
    private String thumbnail; // 혹시 추후 필요하면 사용
    //private List<String> tagNames;
    //private String imageUrl;

    public static BoardListResponseDto from(Board board) {
        return BoardListResponseDto.builder()
                .id(board.getId())
                .content(board.getContent())
                .restaurantName(board.getRestaurant().getName())
                .restaurantAddress(board.getRestaurant().getAddress())
                .imageUrl(board.getBoardImages().isEmpty()
                        ? null
                        : "/images/" + board.getBoardImages().get(0).getStoreFileName())
                .tagNames(
                        board.getBoardTags().stream()
                                .map(tag -> tag.getTag().getName())
                                .toList()
                )
                .build();
    }

    @ArraySchema(schema = @Schema(type = "string"))
    private List<String> tagNames;

    private String imageUrl;
}