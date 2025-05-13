package com.goorm.tablepick.domain.board.dto.response;

import com.goorm.tablepick.domain.board.entity.Board;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
public class PagedBoardsResponseDto {
    private List<BoardSearchResponseDto> boardSearchResponseDtoList;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private long startNumber; // 첫 번호 (1-based)
    private long endNumber;   // 끝 번호

    public PagedBoardsResponseDto(Page<Board> page) {
        this.boardSearchResponseDtoList = page.getContent().stream()
                .map(BoardSearchResponseDto::toDto)
                .collect(Collectors.toList());
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.startNumber = (long) pageNumber * pageSize + 1;
        this.endNumber = startNumber + page.getNumberOfElements() - 1;
    }

}
