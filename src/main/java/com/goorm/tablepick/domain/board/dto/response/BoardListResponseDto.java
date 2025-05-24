package com.goorm.tablepick.domain.board.dto.response;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardImage;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Objects;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardListResponseDto {

    private Long id;
    private String content;
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantCategoryName;
    private String memberNickname;
    private String memberProfileImage;

    @ArraySchema(schema = @Schema(type = "string"))
    private List<String> tagNames;

    private String imageUrl;

    public static BoardListResponseDto from(Board board) {

        var reservation = board.getReservation();
        var restaurant = (reservation != null) ? reservation.getRestaurant() : null;

        return BoardListResponseDto.builder()
                .id(board.getId())
                .content(board.getContent())
                .restaurantName(restaurant != null ? restaurant.getName() : null)
                .restaurantAddress(restaurant != null ? restaurant.getAddress() : null)
                .restaurantCategoryName(
                        restaurant != null && restaurant.getRestaurantCategory() != null
                                ? restaurant.getRestaurantCategory().getName()
                                : null
                )
                .memberNickname(board.getMember().getNickname())
                .memberProfileImage(board.getMember().getProfileImage())
                .imageUrl(
                        board.getBoardImages().stream()
                                .map(BoardImage::getImageUrl)
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null)
                )
                .tagNames(
                        board.getBoardTags().stream()
                                .map(tag -> tag.getTag().getName())
                                .filter(Objects::nonNull)
                                .toList()
                )
                .build();
    }
}
