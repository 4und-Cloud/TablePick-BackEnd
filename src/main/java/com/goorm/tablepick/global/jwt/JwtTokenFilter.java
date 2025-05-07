package com.goorm.tablepick.global.jwt;

import com.goorm.tablepick.domain.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
        String accessToken = resolveToken(request);
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
        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken != null && !jwtProvider.validateToken(refreshToken)) {
            Long userId = jwtProvider.getUserIdFromToken(refreshToken);
            String newRefreshToken = jwtTokenService.handleExpiredRefreshToken(userId, refreshToken);
            if (newRefreshToken != null) {
                // AccessToken 재발급
                String newAccessToken = jwtProvider.createAccessToken(userId);
                response.setHeader("Access-Token", newAccessToken);
                setAuthentication(newAccessToken, request);

                // 새 RefreshToken을 헤더에 담아 전송
                response.setHeader("Refresh-Token", newRefreshToken);
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

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
