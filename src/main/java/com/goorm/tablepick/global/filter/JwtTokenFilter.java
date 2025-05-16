package com.goorm.tablepick.global.filter;

import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.global.jwt.JwtProvider;
import com.goorm.tablepick.global.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final JwtTokenService jwtTokenService; // 서비스 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String accessToken = getAccessTokenFromCookie(request);

        if (accessToken != null) {
            if (jwtProvider.validateToken(accessToken)) {
                setAuthentication(accessToken, request);
            } else {
                handleExpiredAccessToken(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    // accessToken 만료 시
    protected void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken != null && !jwtProvider.validateToken(refreshToken)) {
            Long userId = jwtProvider.getUserIdFromToken(refreshToken);
            String email = jwtProvider.getEmailFromToken(refreshToken);
            String newRefreshToken = jwtTokenService.handleExpiredRefreshToken(userId, email, refreshToken);
            if (newRefreshToken != null) {
                // AccessToken 재발급
                String newAccessToken = jwtProvider.createAccessToken(userId, email);
                Cookie accessCookie = new Cookie("access_token", newAccessToken);
                accessCookie.setPath("/");
                accessCookie.setMaxAge(7 * 24 * 60 * 60);

                // 새 RefreshToken을 쿠키에 담아 전송
                Cookie refreshCookie = new Cookie("refresh_token", newRefreshToken);
                refreshCookie.setHttpOnly(true);
//                refreshCookie.setSecure(true); // https 일 때만
                refreshCookie.setPath("/");
                refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
                response.addCookie(refreshCookie);
            }
        }
    }

    // security context에 인증 객체 저장
    private void setAuthentication(String token, HttpServletRequest request) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        var member = memberRepository.findById(userId).orElse(null);
        if (member != null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    member, null, Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRoles())));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

    // 쿠키에서 액세스 토큰 가져오기
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
