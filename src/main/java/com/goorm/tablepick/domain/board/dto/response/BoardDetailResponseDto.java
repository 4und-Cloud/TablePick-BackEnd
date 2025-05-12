package com.goorm.tablepick.domain.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor

public class BoardDetailResponseDto {
    private String restaurantName;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
    private List<String> tagNames;
    private String content;
    private String memberNickname;
}
