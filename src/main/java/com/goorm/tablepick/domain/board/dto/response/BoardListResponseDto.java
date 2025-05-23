package com.goorm.tablepick.domain.board.dto.response;

import com.goorm.tablepick.domain.board.entity.Board;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Objects;

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

    private String restaurantCategoryName; // 식당 카테고리
    private String memberNickname;         // 작성자 이름
    private String memberProfileImage;  // 작성자 프로필 이미지

    @ArraySchema(schema = @Schema(type = "string"))
    private List<String> tagNames;

    private String imageUrl;   // 게시글 대표 이미지

    public static BoardListResponseDto from(Board board) {

        // 안전하게 Reservation과 Restaurant 꺼내기
        var reservation = board.getReservation();
        var restaurant = (reservation != null) ? reservation.getRestaurant() : null;

        return BoardListResponseDto.builder()
                .id(board.getId())
                .content(board.getContent())

                // null 체크 추가
                .restaurantName(restaurant != null ? restaurant.getName() : null)
                .restaurantAddress(restaurant != null ? restaurant.getAddress() : null)

                // ✅ null 체크 추가: restaurantCategory도 존재할 경우만 접근
                .restaurantCategoryName(
                        restaurant != null && restaurant.getRestaurantCategory() != null
                                ? restaurant.getRestaurantCategory().getName()
                                : null
                )
                // 작성자 닉네임
                .memberNickname(board.getMember().getNickname())
                // 작성자 프로필 이미지 경로
                .memberProfileImage(board.getMember().getProfileImage())

                // 이미지 URL 처리: imageUrl → 없으면 storeFileName
                .imageUrl(
                        board.getBoardImages().stream()
                                .map(image -> {
                                    if (image.getImageUrl() != null) return image.getImageUrl();
                                    return image.getStoreFileName();  // fallback
                                })
                                .filter(Objects::nonNull)
                                .findFirst()
                                .orElse(null)
                )

                // 게시글 태그 이름 리스트
                .tagNames(
                        board.getBoardTags().stream()
                                .map(tag -> tag.getTag().getName())
                                .filter(Objects::nonNull)
                                .toList()
                )
                .build();
    }
}