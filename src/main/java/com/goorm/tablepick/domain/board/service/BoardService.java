package com.goorm.tablepick.domain.board.service;


import com.goorm.tablepick.domain.board.dto.request.BoardCategorySearchRequestDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardsResponseDto;
import com.goorm.tablepick.domain.member.entity.Member;
import jakarta.validation.Valid;

import java.util.List;

public interface BoardService {
    List<BoardListResponseDto> getBoardsForMainPage();

    List<BoardListResponseDto> getBoardList();
    PagedBoardListResponseDto getBoards(int page, int size);


    BoardDetailResponseDto getBoardDetail(Long boardId);

    Long createBoard(BoardRequestDto dto, Member member);

    void updateBoard(Long boardId, BoardRequestDto dto, Member member);

    void deleteBoard(Long boardId, Member member);

    PagedBoardsResponseDto searchAllByCategory(@Valid BoardCategorySearchRequestDto boardSearchRequestDto);
      
}