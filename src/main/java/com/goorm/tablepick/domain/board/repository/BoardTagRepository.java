package com.goorm.tablepick.domain.board.repository;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.board.entity.BoardTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardTagRepository extends JpaRepository<BoardTag, Long> {

    @Query("SELECT DISTINCT bt.board FROM BoardTag bt WHERE bt.tag.name IN :tagNames")
    List<Board> findBoardsByTagNames(List<String> tagNames);

    @Query("SELECT DISTINCT bt.board FROM BoardTag bt")
    List<Board> findAllBoards();

    void deleteByBoardId(Long boardId); // 게시물 수정 시 기존 태그 삭제용
}
