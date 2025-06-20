package com.goorm.tablepick.domain.boardLike.service;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.exception.BoardErrorCode;
import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.boardLike.entity.BoardLike;
import com.goorm.tablepick.domain.boardLike.repository.BoardLikeRepository;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.exception.MemberErrorCode;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.global.exception.BoardException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardLikeServiceImpl implements  BoardLikeService {
    private final MemberRepository memberRepository;
    private final BoardRepository boardRepository;
    private final BoardLikeRepository boardLikeRepository;

    @Override
    @Transactional
    public void LikeBoard(Long memberId, Long BoardId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        Board board = boardRepository.findById(BoardId).orElseThrow(() -> new BoardException(BoardErrorCode.NOT_FOUND));

        Optional<BoardLike> boardLike = boardLikeRepository.findByMemberAndBoard(member, board);

        if(boardLike.isPresent()){
            boardLikeRepository.delete(boardLike.get());
        }else{
            BoardLike newBoardLike = BoardLike.builder().board(board).member(member).build();
            boardLikeRepository.save(newBoardLike);
        }
    }

    public long countLikes(Board board) {
        return boardLikeRepository.countByBoard(board);
    }

}
