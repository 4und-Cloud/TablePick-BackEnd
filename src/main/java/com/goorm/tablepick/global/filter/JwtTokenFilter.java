package com.goorm.tablepick.global.filter;

import com.goorm.tablepick.domain.member.exception.MemberErrorCode;
import com.goorm.tablepick.domain.member.exception.MemberException;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.global.jwt.JwtProvider;
import com.goorm.tablepick.global.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (shouldSkipFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = getCookieValue(request, "access_token");

        try {
            if (accessToken != null && jwtProvider.validateToken(accessToken)) {
                setAuthentication(accessToken, request);
            } else {
                log.warn("❌ 유효하지 않거나 만료된 access token");
                handleUnauthorized(response);
                return;
            }
        } catch (Exception e) {
            log.error("🔥 JWT 필터 처리 중 예외 발생: {}", e.getMessage(), e);
            handleUnauthorized(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String token, HttpServletRequest request) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        var member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.NOT_FOUND));

        var userDetails = new CustomUserDetails(member);
        var auth = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRoles()))
        );
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);

        log.info("✅ 사용자 인증 성공 - userId: {}", userId);
    }

    private boolean shouldSkipFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        return path.contains("/swagger-ui") ||
                path.contains("/v3/api-docs") ||
                path.equals("/oauth2/success") ||
                path.equals("/login") ||
                path.equals("/api/boards/list") ||
                path.equals("/api/boards/main") ||
                path.equals("/api/boards/{boardId}") ||
                path.equals("/api/tags") ||
                path.equals("/favicon") ||
                path.startsWith("/api/restaurants/") ||
                path.startsWith("/api/boards/") ||
                path.startsWith("/api/boards/restaurant/") ||
                path.startsWith("/oauth2/") ||               // 카카오, 구글 로그인 인증 중간 경로
                path.startsWith("/login/oauth2/code/");
    }

    private void handleUnauthorized(HttpServletResponse response) {
        clearTokenCookies(response);
        SecurityContextHolder.clearContext();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    private void clearTokenCookies(HttpServletResponse response) {
        deleteCookie(response, "access_token");
        deleteCookie(response, "refresh_token");
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}