package com.goorm.tablepick.domain.board.controller;

import com.goorm.tablepick.domain.board.service.BoardTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board-tags")
public class BoardTagController {

    private final BoardTagService boardTagService;

    @PostMapping(value = "/{boardId}")
    @Operation(summary = "게시글 태그 수정", description = "게시글 ID와 태그 ID 목록을 전달하여 게시글의 태그를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "태그 수정 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<Void> updateBoardTags(
            @PathVariable @Parameter(description = "게시글 ID") Long boardId,
            @RequestBody @Parameter(description = "태그 ID 목록") List<Long> tagIds
    ) {
        boardTagService.updateBoardTags(boardId, tagIds);
        return ResponseEntity.ok().build();
    }
}