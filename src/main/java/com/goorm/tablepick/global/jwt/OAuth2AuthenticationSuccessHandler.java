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

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String accessToken = request.getHeader("Access-Token");
        String refreshToken = getRefreshTokenFromCookie(request);
        String email = extractEmail(attributes);
        // 사용자 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("인증 후 사용자 정보가 없습니다."));

        authenticateUser(member);

        // 액세스 토큰 유효성 검사 및 재발급
        if (accessToken == null || !jwtProvider.validateToken(accessToken)) {
            accessToken = jwtProvider.createAccessToken(member.getId(), email);

            // 리프레시 토큰 유효성 검사 및 재발급
            if (refreshToken == null || !jwtProvider.validateToken(refreshToken)) {
                refreshToken = jwtProvider.createRefreshToken(member.getId(), email);
                issueAndSaveRefreshToken(member, refreshToken);
            }
        }


        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(7 * 24 * 60 * 60);
      
        // 리프레시 토큰을 쿠키에 설정
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
//        refreshCookie.setSecure(true); // HTTPS에서만 전송
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일

        response.addCookie(refreshCookie);
        response.addCookie(accessCookie);

        String redirectUrl = "http://localhost:5173/oauth2/success";
        response.sendRedirect(redirectUrl);
    }

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
    private void issueAndSaveRefreshToken(Member member, String refreshTokenString) {
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(7);
        refreshTokenRepository.deleteByToken(refreshTokenString);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .expiredAt(expiredAt)
                .member(member)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    // 쿠키에서 리프레쉬 토큰 가져오기
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
