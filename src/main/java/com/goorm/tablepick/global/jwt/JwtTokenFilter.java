package com.goorm.tablepick.global.jwt;

import com.goorm.tablepick.domain.member.entity.RefreshToken;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.member.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;

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
    private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String refreshToken = request.getHeader("Refresh-Token");

        if (refreshToken != null && jwtProvider.validateToken(refreshToken)) {
            Long userId = jwtProvider.getUserIdFromToken(refreshToken);
            RefreshToken storedToken = refreshTokenRepository.findByMemberId(userId).orElse(null);
            if (storedToken != null && storedToken.getToken().equals(refreshToken)) {
                // AccessToken 재발급
                String newAccessToken = jwtProvider.createAccessToken(userId);
                response.setHeader("Access-Token", newAccessToken);
                setAuthentication(newAccessToken, request);

                // RefreshToken 만료 시 갱신
                //if (jwtProvider.isTokenExpiringSoon(refreshToken)) {
                //}
            }
        }
    }

    //security context에 인증 객체 저장
    private void setAuthentication(String token, HttpServletRequest request) {
        Long userId = jwtProvider.getUserIdFromToken(token);
        var member = memberRepository.findById(userId).orElse(null);
        if (member != null) {
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    member, null, Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRoles())));
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 수정됨
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
