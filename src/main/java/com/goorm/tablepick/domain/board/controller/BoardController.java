package com.goorm.tablepick.domain.board.controller;

import com.goorm.tablepick.domain.board.dto.request.BoardCategorySearchRequestDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardsResponseDto;
import com.goorm.tablepick.domain.board.service.BoardService;
import com.goorm.tablepick.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    @Operation(summary = "게시글 생성", description = "로그인된 사용자가 게시글을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 ID 반환", content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<?> createBoard(
            @ModelAttribute @Parameter(description = "게시글 생성 정보") BoardRequestDto dto,
            @AuthenticationPrincipal Member member  // 여기 수정됨
    ) {
        Long boardId = boardService.createBoard(dto, member);
        return ResponseEntity.ok(boardId);
    }

    @GetMapping("/{boardId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 게시글 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 정보 반환", content = @Content(schema = @Schema(implementation = BoardDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<BoardDetailResponseDto> getBoardDetail(
            @PathVariable @Parameter(description = "게시글 ID") Long boardId) {
        return ResponseEntity.ok(boardService.getBoardDetail(boardId));
    }

    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "게시글 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "게시글 목록 반환", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BoardListResponseDto.class))))
    public List<BoardListResponseDto> getBoards() {
        return boardService.getBoardList();
    }


    @GetMapping("/search/category")
    @Operation(summary = "게시글 카테고리 검색", description = "카테고리로 게시글 내용을 통해 게시글을 검색합니다.")
    public ResponseEntity<PagedBoardsResponseDto> searchBoards(
            @ModelAttribute @Valid BoardCategorySearchRequestDto boardSearchRequestDto) {
        PagedBoardsResponseDto pagedBoardsResponseDto = boardService.searchAllByCategory(boardSearchRequestDto);

        return ResponseEntity.ok(pagedBoardsResponseDto);
    }

}