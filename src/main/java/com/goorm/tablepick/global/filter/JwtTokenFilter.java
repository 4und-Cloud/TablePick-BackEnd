package com.goorm.tablepick.global.filter;

import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.global.jwt.JwtProvider;
import com.goorm.tablepick.global.jwt.JwtTokenService;
import com.goorm.tablepick.global.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Swagger UI 관련 경로는 필터 처리 건너뛰기
        String path = request.getRequestURI();
        if (path.contains("/swagger-ui") || path.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getAccessTokenFromCookie(request);

        log.info("🪪 [JwtTokenFilter] Extracted Access Token: {}", accessToken);

        try {
            if (accessToken != null) {
                if (jwtProvider.validateToken(accessToken)) {
                    log.info("✅ [JwtTokenFilter] JWT 유효성 검사 통과");
                    setAuthentication(accessToken, request);
                } else {
                    log.warn("❌ [JwtTokenFilter] JWT 유효성 검사 실패 또는 만료됨");
                    handleExpiredAccessToken(request, response);
                    return;
                }
            } else {
                log.warn("🚫 [JwtTokenFilter] Authorization 헤더가 없거나 Bearer 형식 아님");
            }
        } catch (Exception e) {
            log.error("🔥 [JwtTokenFilter] 필터 처리 중 예외 발생: {}", e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token, HttpServletRequest request) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        var member = memberRepository.findById(userId).orElse(null);

        log.info("🔐 [JwtTokenFilter] 사용자 인증 시도 - userId: {}, member: {}", userId, member);

        if (member != null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    new CustomUserDetails(member), null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRoles()))
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("✅ [JwtTokenFilter] SecurityContext 인증 완료");
        } else {
            log.warn("❌ [JwtTokenFilter] userId로 멤버 조회 실패 - 인증 실패");
        }
    }

    protected void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String refreshToken = getRefreshTokenFromCookie(request);
        log.warn("♻️ [JwtTokenFilter] accessToken 만료 - refreshToken 시도: {}", refreshToken);

        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            Long userId = jwtProvider.getUserIdFromToken(refreshToken);
            String email = jwtProvider.getEmailFromToken(refreshToken);
            String newRefreshToken = jwtTokenService.handleExpiredRefreshToken(userId, email, refreshToken);

            if (newRefreshToken != null) {
                String newAccessToken = jwtProvider.createAccessToken(userId, email);
                Cookie accessCookie = new Cookie("access_token", newAccessToken);
                accessCookie.setPath("/");
                accessCookie.setMaxAge(24 * 60 * 60);

                Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
                refreshCookie.setHttpOnly(true);
                refreshCookie.setSecure(true);
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge(7 * 24 * 60 * 60);
                response.addCookie(refreshCookie);

                log.info("♻️ [JwtTokenFilter] Access/Refresh 재발급 성공");
            } else {
                log.warn("❌ [JwtTokenFilter] RefreshToken으로도 재발급 실패");
            }
        } else {
            log.warn("🚫 [JwtTokenFilter] RefreshToken 없음 또는 유효하지 않음");
        }
    }

    private String getAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

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
