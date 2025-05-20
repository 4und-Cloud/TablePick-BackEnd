package com.goorm.tablepick.domain.board.controller;

import com.goorm.tablepick.domain.board.dto.request.BoardCategorySearchRequestDto;
import com.goorm.tablepick.domain.board.dto.request.BoardCreateResponseDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardsResponseDto;
import com.goorm.tablepick.domain.board.service.BoardService;
import com.goorm.tablepick.domain.member.entity.Member;
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

    // 게시글 목록 페이지. 랜딩 페이지. 4개시씩 보여지는 화면. 게시글 이미지는 1개만.
    @GetMapping("/main")
    public List<BoardListResponseDto> getMainBoards() {
        return boardService.getBoardsForMainPage();
    }

    // "게시물 더보기"를 누르면. 게시물만 한 화면에 6개씩 페이지네이션 해서 보여짐.
    @GetMapping("/list")
    public PagedBoardListResponseDto getBoards(
            @RequestParam(defaultValue = "1") int page,  // [수정] 기본값 0 → 1
            @RequestParam(defaultValue = "6") int size

    ) {
        return boardService.getBoards(page, size);
    }

    // 게시글 상세 페이지. 기존에 있던 게시물을 불러오는 거. 많아봐야 이미지 2, 3개라고 함.
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

    // 게시글 작성 페이지, 이미지는 0개~3개 첨부 가능. 태그 선택은 1개에서 5개.
    @Operation(summary = "게시글 생성", description = "로그인된 사용자가 게시글을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBoard(
            @RequestPart("data") BoardRequestDto dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal Member member
    ) {
        dto.setImages(images);

        log.info("🙋‍♂️ [createBoard] 요청자 인증된 멤버: {}", member);

        // 유효성 검사
        if (images != null && images.size() > 3) {
            return ResponseEntity.badRequest()
                    .body("이미지는 최대 3개까지 업로드 가능합니다.");
        }
        if (dto.getTagNames() == null || dto.getTagNames().size() < 1 || dto.getTagNames().size() > 5) {
            return ResponseEntity.badRequest().body("태그는 최소 1개, 최대 5개까지 등록 가능합니다.");
        }

        BoardCreateResponseDto response = boardService.createBoard(dto, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 게시글 수정 페이지
    @PutMapping("/{boardId}")
    @Operation(summary = "게시글 수정", description = "게시글 ID를 통해 기존 게시글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "작성자 본인만 수정 가능"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    public ResponseEntity<Void> updateBoard(
            @PathVariable @Parameter(description = "게시글 ID") Long boardId,
            @ModelAttribute @Parameter(description = "수정할 게시글 정보") BoardRequestDto dto,
            @AuthenticationPrincipal Member member
    ) {
        boardService.updateBoard(boardId, dto, member);
        return ResponseEntity.ok().build();
    }

    // 이건 뭔지. 필요 없어 보임. 확인 후 삭제 예정.
    @GetMapping
    public ResponseEntity<?> getBoards() {
        List<BoardListResponseDto> boards = boardService.getBoardList();
        if (boards.isEmpty()) {
            return ResponseEntity.noContent().build();  // 204 No Content
        }
        return ResponseEntity.ok(boards); // 200 OK
    }

    // 게시글 삭제 페이지
    @DeleteMapping("/{boardId}")
    @Operation(summary = "게시글 삭제", description = "게시글 ID를 통해 해당 게시글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "작성자 본인만 삭제 가능")
    })
    public ResponseEntity<Void> deleteBoard(
            @PathVariable @Parameter(description = "게시글 ID") Long boardId,
            @AuthenticationPrincipal Member member
    ) {
        boardService.deleteBoard(boardId, member);
        return ResponseEntity.ok().build(); // 200 OK 반환
    }
    // 게시글 검색 페이지
    @GetMapping("/search/category")
    @Operation(summary = "게시글 카테고리 검색", description = "카테고리로 게시글 내용을 통해 게시글을 검색합니다.")
    public ResponseEntity<PagedBoardsResponseDto> searchBoards(
            @ModelAttribute @Valid BoardCategorySearchRequestDto boardSearchRequestDto) {
        PagedBoardsResponseDto pagedBoardsResponseDto = boardService.searchAllByCategory(boardSearchRequestDto);

        return ResponseEntity.ok(pagedBoardsResponseDto);
    }
}