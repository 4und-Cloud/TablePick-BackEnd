package com.goorm.tablepick.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


// Firebase Service Worker 파일 요청에 대한 보안 및 캐시 관련 헤더를 추가하는 필터
// Service Worker의 정상적인 동작과 보안을 위한 필수 헤더들을 설정
@Component
public class ServiceWorkerHeaderFilter extends OncePerRequestFilter {

    // Service Worker 파일 경로
    private static final String SERVICE_WORKER_PATH = "/firebase-messaging-sw.js";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Service Worker 파일 요청인 경우에만 헤더 추가
        if (request.getRequestURI().endsWith(SERVICE_WORKER_PATH)) {
            // Service Worker의 스코프 설정 (루트 경로에서 동작 가능하도록 설정)
            response.setHeader("Service-Worker-Allowed", "/");

            // 브라우저 캐싱 방지를 위한 헤더 설정
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }

        // 다음 필터 체인으로 요청 전달
        filterChain.doFilter(request, response);
    }
}
