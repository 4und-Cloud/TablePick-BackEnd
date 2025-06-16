package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.dto.response.BoardCreateResponseDto;
import com.goorm.tablepick.domain.board.dto.request.BoardRequestDto;
import com.goorm.tablepick.domain.board.dto.response.BoardDetailResponseDto;
import com.goorm.tablepick.domain.board.dto.response.BoardListResponseDto;
import com.goorm.tablepick.domain.board.dto.response.PagedBoardListResponseDto;
import com.goorm.tablepick.domain.member.entity.Member;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BoardService {
    PagedBoardListResponseDto getBoards(int page, int size, Member member);
    BoardDetailResponseDto getBoardDetail(Long boardId, Member member);
    List<BoardListResponseDto> getBoardsByRestaurant(Long restaurantId);

    BoardCreateResponseDto createBoard(BoardRequestDto dto, List<MultipartFile> images, Member member);
    void deleteBoard(Long boardId, Member member);
}