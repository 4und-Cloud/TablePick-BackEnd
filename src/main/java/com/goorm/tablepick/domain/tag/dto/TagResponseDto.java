package com.goorm.tablepick.domain.tag.dto;

import com.goorm.tablepick.domain.tag.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TagResponseDto {
    @Schema(description = "태그 ID", example = "1")
    private Long id;
    @Schema(description = "태그 이름", example = "분위기가 좋아요")
    private String name;

    public static TagResponseDto toDto(Tag tag) {
        return TagResponseDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
