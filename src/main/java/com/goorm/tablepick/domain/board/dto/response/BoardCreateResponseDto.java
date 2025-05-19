package com.goorm.tablepick.domain.board.dto.response;

import java.time.LocalDateTime;

public class BoardCreateResponseDto {
    private Long boardId;
    private LocalDateTime createdAt;
    private Long memberId;
    private Long restaurantId;
    private String content;
}
