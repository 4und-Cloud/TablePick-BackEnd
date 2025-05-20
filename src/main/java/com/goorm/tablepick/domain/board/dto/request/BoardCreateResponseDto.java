package com.goorm.tablepick.domain.board.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class BoardCreateResponseDto {
    private Long boardId;
    private String content;
    private List<String> imageUrls;
    private List<String> tags;
    private String writerNickname;
    private String writerProfileImageUrl;
    private LocalDateTime createdAt;
}
