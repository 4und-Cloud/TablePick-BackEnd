package com.goorm.tablepick.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 관련 설정을 위한 구성 클래스
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .exposedHeaders("Access-Token", "Refresh-Token");
    }

    // Firebase 서비스 워커를 위한 리소스 핸들러 추가
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/firebase-messaging-sw.js")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);  // 캐싱 비활성화
    }

    // Firebase 서비스 워커를 위한 필터 빈 추가
//    @Bean
//    public OncePerRequestFilter serviceWorkerHeaderFilter() {
//        return new OncePerRequestFilter() {
//            @Override
//            protected void doFilterInternal(HttpServletRequest request,
//                                            HttpServletResponse response,
//                                            FilterChain filterChain)
//                    throws ServletException, IOException {
//
//                // firebase-messaging-sw.js 요청에 대해서만 헤더 추가
//                if (request.getRequestURI().contains("firebase-messaging-sw.js")) {
//                    response.setHeader("Service-Worker-Allowed", "/");
//                }
//
//                filterChain.doFilter(request, response);
//            }
//        };
//    }
}
