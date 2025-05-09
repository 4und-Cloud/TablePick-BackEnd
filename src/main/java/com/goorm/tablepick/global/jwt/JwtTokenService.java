package com.goorm.tablepick.global.jwt;

import com.goorm.tablepick.domain.member.entity.RefreshToken;
import com.goorm.tablepick.domain.member.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtTokenService(JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository) {
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public String handleExpiredRefreshToken(Long userId, String email, String refreshToken) {
        // RefreshToken이 존재하는지 확인
        RefreshToken storedToken = refreshTokenRepository.findByMemberId(userId).orElse(null);

        if (storedToken != null && storedToken.getToken().equals(refreshToken)) {
            // RefreshToken이 유효하면 갱신
            if (!jwtProvider.validateToken(refreshToken)) {
                String newRefreshToken = jwtProvider.createRefreshToken(userId, email);
                storedToken.updateToken(newRefreshToken, LocalDateTime.now().plusDays(7)); // 토큰 갱신
                refreshTokenRepository.save(storedToken);  // 변경 사항을 DB에 반영
                return newRefreshToken;
            }
        }

        return null; // RefreshToken이 만료되지 않았거나 유효하지 않음
    }
}