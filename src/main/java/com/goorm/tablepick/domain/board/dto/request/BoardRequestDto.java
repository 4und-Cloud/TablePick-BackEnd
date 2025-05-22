package com.goorm.tablepick.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Schema(description = "게시글 생성 요청")
public class BoardRequestDto {

    @NotNull(message = "예약 ID는 필수입니다.") // 추가
    @Schema(description = "예약 ID", example = "5") // 예약 기반으로 게시글 작성
    private Long reservationId; // 수정된 설계에 따라 추가됨

    @NotBlank
    @Schema(description = "게시글 내용", example = "정말 맛있는 집이에요.")
    private String content;

    @Size(min = 1, max = 5)
    @Schema(description = "태그 리스트", example = "[\"조용해요\", \"가성비 좋아요\"]")
    private List<String> tagNames; // 태그 문자열 리스트

    private List<MultipartFile> images; // 이건 @RequestPart에서 set 해줘야 함
}
