package com.goorm.tablepick.domain.board.repository;

import com.goorm.tablepick.domain.board.entity.BoardKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardKeywordRepository extends JpaRepository<BoardKeyword, Long> {
}
