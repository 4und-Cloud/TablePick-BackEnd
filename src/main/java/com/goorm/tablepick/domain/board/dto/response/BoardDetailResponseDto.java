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

    public static BoardDetailResponseDto from(Board board) {
        // Reservation과 Restaurant null 체크
        Reservation reservation = board.getReservation();
        Restaurant restaurant = reservation != null ? reservation.getRestaurant() : null;

        Member member = board.getMember();

        List<String> imageUrls = board.getBoardImages().stream()
                .map(BoardImage::getImageUrl)
                .filter(Objects::nonNull)
                .limit(3) // 최대 3장으로 제한
                .toList();

        List<String> tagNames = board.getBoardTags().stream()
                .map(boardTag -> boardTag.getTag().getName())
                .filter(Objects::nonNull)
                .toList();

        // NullPointer 방지
        String restaurantCategoryName = (restaurant != null && restaurant.getRestaurantCategory() != null)
                ? restaurant.getRestaurantCategory().getName()
                : null;

        String restaurantName = restaurant != null ? restaurant.getName() : null;
        String restaurantAddress = restaurant != null ? restaurant.getAddress() : null;

        // 작성일 null 방지 + 포맷
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
