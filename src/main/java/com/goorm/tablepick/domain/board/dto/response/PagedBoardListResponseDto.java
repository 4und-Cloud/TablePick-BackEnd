package com.goorm.tablepick.domain.board.dto.response;

import com.goorm.tablepick.domain.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@AllArgsConstructor
public class PagedBoardListResponseDto {
    private List<BoardListResponseDto> boardList;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private long startNumber;
    private long endNumber;

    public PagedBoardListResponseDto(Page<Board> page) {
        this.boardList = page.getContent().stream()
                .map(BoardListResponseDto::from)
                .filter(dto -> dto.getImageUrl() != null)  // 추가
                .toList();
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.startNumber = (long) pageNumber * pageSize + 1;
        this.endNumber = startNumber + page.getNumberOfElements() - 1;
    }
}
