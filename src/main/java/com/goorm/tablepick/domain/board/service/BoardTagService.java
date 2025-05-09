package com.goorm.tablepick.domain.board.service;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardTag;
import com.goorm.tablepick.domain.board.repository.BoardRepository;
import com.goorm.tablepick.domain.board.repository.BoardTagRepository;
import com.goorm.tablepick.domain.tag.entity.Tag;
import com.goorm.tablepick.domain.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardTagService {

    private final BoardTagRepository boardTagRepository;
    private final BoardRepository boardRepository;
    private final TagRepository tagRepository;

    @Transactional
    public void updateBoardTags(Long boardId, List<Long> tagIds) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        // 기존 태그 삭제
        boardTagRepository.deleteByBoardId(boardId);

        // 새 태그 추가
        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new IllegalArgumentException("태그가 존재하지 않습니다."));
            BoardTag boardTag = new BoardTag(board, tag);
            boardTagRepository.save(boardTag);
        }
    }
}


