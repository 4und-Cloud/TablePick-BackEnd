package com.goorm.tablepick.global.jwt;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.entity.RefreshToken;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.member.repository.RefreshTokenRepository;
import com.goorm.tablepick.global.security.CustomUserDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        String email = extractEmail(oAuth2User.getAttributes());

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("인증 후 사용자 정보가 없습니다."));

        // 인증 객체 등록
        authenticateUser(member);

        // 항상 access/refresh 토큰 재발급
        String accessToken = jwtProvider.createAccessToken(member.getId(), email);
        String refreshToken = issueAndSaveRefreshToken(member).getToken();

        // 쿠키 설정
        response.addCookie(createAccessCookie(accessToken));
        response.addCookie(createRefreshCookie(refreshToken));

        // 리다이렉션
        response.sendRedirect("http://localhost:5173/oauth2/success");
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(Map<String, Object> attributes) {
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        }
        return (String) attributes.get("email");
    }

    private void authenticateUser(Member member) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(member.getEmail());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    //refresh 토큰 발급 및 db 저장

    private RefreshToken issueAndSaveRefreshToken(Member member) {
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);
        RefreshToken refreshToken = refreshTokenRepository.findByMember(member).orElse(null);

        String newRefreshToken = jwtProvider.createRefreshToken(member.getId(), member.getEmail());

        if (refreshToken != null) {
            refreshToken.updateToken(newRefreshToken, expiredAt);
            return refreshToken;
        } else {
            RefreshToken createdRefreshToken = RefreshToken.builder()
                    .token(newRefreshToken)
                    .expiredAt(expiredAt)
                    .member(member)
                    .build();
            return refreshTokenRepository.save(createdRefreshToken);
        }
    }


    private Cookie createAccessCookie(String accessToken) {
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");

        return accessCookie;
    }

    private Cookie createRefreshCookie(String refreshToken) {
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
//        refreshCookie.setSecure(true); // HTTPS에서만 전송
        refreshCookie.setPath("/");

        return refreshCookie;
    }
}
