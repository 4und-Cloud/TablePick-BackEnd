package com.goorm.tablepick.global.jwt;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.entity.RefreshToken;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.member.repository.RefreshTokenRepository;
import com.goorm.tablepick.global.security.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        String email = extractEmail(attributes);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("인증 후 사용자 정보가 없습니다."));

        authenticateUser(member);

        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        issueAndSaveRefreshToken(member, refreshToken);
        System.out.println(accessToken);
        // 토큰을 헤더에 설정
        response.setHeader("Access-Token", accessToken);
        response.setHeader("Refresh-Token", refreshToken);
        response.setHeader("Access-Control-Expose-Headers", "Access-Token, Refresh-Token");

        // 상태 코드만 전달 (200 OK)
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Login Success");
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
        LocalDateTime expiredAt = jwtProvider.getExpiration(refreshTokenString)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenString)
                .deviceInfo("web")
                .expiredAt(expiredAt)
                .build();

        refreshToken.setMember(member);
        refreshTokenRepository.save(refreshToken);
    }
}