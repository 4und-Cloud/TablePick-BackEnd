package com.goorm.tablepick.domain.board.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Schema(description = "게시글 생성 요청")
//@Data
public class BoardRequestDto {

    @Schema(description = "식당 ID", example = "1")
    private Long restaurantId;

    @Schema(description = "게시글 내용", example = "정말 맛있는 집이에요.")
    private String content;

    @Schema(description = "태그 리스트", example = "[\"조용해요\", \"가성비 좋아요\"]")
    private List<String> tagNames; // 태그 문자열 리스트

    private List<MultipartFile> images; // 이건 @RequestPart에서 set 해줘야 함

//    public void setImages(List<MultipartFile> images) {
//        this.images = images;
//    }
    // 이미지 필드는 @RequestPart로 따로 받기 때문에 여기서 제외해도 됨
    // 컨트롤러에서 setImages() 해주기 때문에 여기는 제외
    //private transient List<MultipartFile> images; // 이미지 파일 리스트
}
