package com.goorm.tablepick.domain.member.repository;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByMemberId(Long memberId);

    RefreshToken findByMemberEmail(String email);

    Optional<RefreshToken> findByMember(Member member);

    void deleteByToken(String refreshTokenString);
}
