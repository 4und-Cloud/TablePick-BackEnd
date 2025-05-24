package com.goorm.tablepick.domain.board.controller;

import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.*;
import com.goorm.tablepick.domain.board.service.BoardService;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@Slf4j
public class BoardController {

    private final BoardService boardService;


    @GetMapping("/main")
    public List<BoardListResponseDto> getMainBoards() {
        return boardService.getBoardsForMainPage();
    }


    @GetMapping("/list")
    public PagedBoardListResponseDto getPagedBoards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "6") int size

    ) {
        return boardService.getBoards(page, size);
    }


    @GetMapping("/{boardId}")
    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 게시글 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 상세 정보 반환",
                    content = @Content(schema = @Schema(implementation = BoardDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<BoardDetailResponseDto> getBoardDetail(
            @PathVariable @Parameter(description = "게시글 ID") Long boardId) {
        return ResponseEntity.ok(boardService.getBoardDetail(boardId));
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "게시글 생성",
            description = "로그인된 사용자가 게시글을 생성합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<BoardCreateResponseDto> createBoard(
            @Valid @ModelAttribute BoardRequestDto boardRequestDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Member member = userDetails.getMember();

        BoardCreateResponseDto response = boardService.createBoard(boardRequestDto, boardRequestDto.getImages(), member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}