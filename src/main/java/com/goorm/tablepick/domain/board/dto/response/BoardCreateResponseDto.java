package com.goorm.tablepick.domain.board.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BoardCreateResponseDto {
    private Long boardId;
    private String message;
}
