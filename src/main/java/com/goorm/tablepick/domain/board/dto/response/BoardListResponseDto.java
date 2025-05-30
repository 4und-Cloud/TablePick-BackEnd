package com.goorm.tablepick.domain.board.dto.response;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardImage;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import com.goorm.tablepick.domain.restaurant.entity.RestaurantCategory;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor  // 이 어노테이션 추가
public class BoardListResponseDto {
    private Long id;
    private String content;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantCategoryName;
    private String memberNickname;
    private String memberProfileImage;
    private LocalDateTime createdAt;  // 이 필드가 누락되어 있었음

    @ArraySchema(schema = @Schema(type = "string"))
    private List<String> tagNames;

    private String imageUrl;

    public static BoardListResponseDto from(Board board, Restaurant restaurant, RestaurantCategory category) {
        return BoardListResponseDto.builder()
                .id(board.getId())
                .content(board.getContent())
                .restaurantName(restaurant.getName())
                .restaurantAddress(restaurant.getAddress())
                .restaurantCategoryName(category.getName())
                .memberNickname(board.getMember().getNickname())
                .memberProfileImage(board.getMember().getProfileImage())
                .tagNames(board.getBoardTags().stream()
                        .map(boardTag -> boardTag.getTag().getName())
                        .collect(Collectors.toList()))
                .imageUrl(board.getBoardImages().isEmpty() ? null :
                        board.getBoardImages().getFirst().getImageUrl())
                .createdAt(board.getCreatedAt())
                .build();
    }
}
