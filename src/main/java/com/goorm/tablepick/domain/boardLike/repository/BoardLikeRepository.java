package com.goorm.tablepick.domain.boardLike.repository;

import com.goorm.tablepick.domain.board.entity.Board;
import com.goorm.tablepick.domain.boardLike.entity.BoardLike;
import com.goorm.tablepick.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardLikeRepository extends JpaRepository<BoardLike, Long> {
    long countByBoard(Board board);

    Optional<BoardLike> findByMemberAndBoard(Member member, Board board);
}
