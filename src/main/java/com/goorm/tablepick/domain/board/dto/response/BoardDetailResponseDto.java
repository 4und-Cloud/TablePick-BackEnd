package com.goorm.tablepick.domain.board.dto.response;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardImage;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
public class BoardDetailResponseDto {
    private String restaurantName;
    private String restaurantAddress;
    private String restaurantCategoryName;
    private String memberNickname;
    private String memberProfileImage;
    private String content;
    private List<String> tagNames;
    private List<String> imageUrls;
    private String createdAt;

    public static BoardDetailResponseDto from(Board board, Restaurant restaurant) {
        Member member = board.getMember();

        List<String> imageUrls = board.getBoardImages().stream()
                .map(BoardImage::getImageUrl)
                .filter(Objects::nonNull)
                .limit(3)
                .toList();

        List<String> tagNames = board.getBoardTags().stream()
                .map(boardTag -> boardTag.getTag().getName())
                .filter(Objects::nonNull)
                .toList();

        // Restaurant fields from the passed Restaurant entity
        String restaurantCategoryName = (restaurant != null && restaurant.getRestaurantCategory() != null)
                ? restaurant.getRestaurantCategory().getName()
                : null;
        String restaurantName = restaurant != null ? restaurant.getName() : null;
        String restaurantAddress = restaurant != null ? restaurant.getAddress() : null;

        // Format createdAt
        String createdAtStr = board.getCreatedAt() != null
                ? board.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss"))
                : null;

        return BoardDetailResponseDto.builder()
                .restaurantName(restaurantName)
                .restaurantAddress(restaurantAddress)
                .restaurantCategoryName(restaurantCategoryName)
                .memberNickname(member.getNickname())
                .memberProfileImage(member.getProfileImage())
                .content(board.getContent())
                .tagNames(tagNames)
                .imageUrls(imageUrls)
                .createdAt(createdAtStr)
                .build();
    }
}
