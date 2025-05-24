package com.goorm.tablepick.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "게시글 생성 요청")
public class BoardRequestDto {

    @NotNull(message = "예약 ID는 필수입니다.")
    @Schema(description = "예약 ID", example = "5")
    private Long reservationId;

    @NotBlank
    @Schema(description = "게시글 내용", example = "정말 맛있는 집이에요.")
    private String content;

    @Size(min = 1, max = 5)
    @Schema(description = "태그 리스트", example = "[1, 2]")
    private List<Long> tagId;

    @Schema(description = "이미지 파일들")
    private List<MultipartFile> images;

}