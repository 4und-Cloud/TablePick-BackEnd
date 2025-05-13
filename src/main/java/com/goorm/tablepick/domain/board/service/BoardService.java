package com.goorm.tablepick.domain.board.service;


import com.goorm.tablepick.domain.board.dto.request.BoardCategorySearchRequestDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardsResponseDto;
import com.goorm.tablepick.domain.member.entity.Member;
import jakarta.validation.Valid;
import java.util.List;

public interface BoardService {
    Long createBoard(BoardRequestDto dto, Member member);

    BoardDetailResponseDto getBoardDetail(Long boardId);

    List<BoardListResponseDto> getBoardList();

    PagedBoardsResponseDto searchAllByCategory(@Valid BoardCategorySearchRequestDto boardSearchRequestDto);
}