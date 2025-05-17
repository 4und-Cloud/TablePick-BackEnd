package com.goorm.tablepick.domain.tag.controller;

import com.goorm.tablepick.domain.tag.dto.TagResponseDto;
import com.goorm.tablepick.domain.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;

    @GetMapping
    @Operation(summary = "전체 태그 조회", description = "전체 태그을 조회합니다.")
    public ResponseEntity<List<TagResponseDto>> getTags() {
        return ResponseEntity.ok(tagService.getTagList());
    }

}
