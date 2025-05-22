package com.goorm.tablepick.domain.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
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
}
