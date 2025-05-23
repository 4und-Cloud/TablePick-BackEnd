package com.goorm.tablepick.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 관련 설정을 위한 구성 클래스
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${project.upload.path}")
    private String uploadBasePath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Access-Token")
                .exposedHeaders("Access-Token")
                .allowCredentials(true);
    }

    // Firebase 서비스 워커를 위한 리소스 핸들러 추가
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/firebase-messaging-sw.js")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);  // 캐싱 비활성화

        // 업로드된 이미지를 위한 핸들러 추가
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadBasePath + "/");
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
