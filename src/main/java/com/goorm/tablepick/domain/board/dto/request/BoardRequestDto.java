package com.goorm.tablepick.domain.board.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
public class BoardRequestDto {
    private Long restaurantId;
    private String content;
    private List<String> tagNames; // 태그 문자열 리스트
    private List<MultipartFile> images; // 이미지 파일 리스트
}
