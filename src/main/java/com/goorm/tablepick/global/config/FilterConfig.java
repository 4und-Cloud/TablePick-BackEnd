package com.goorm.tablepick.global.config;

import jakarta.servlet.DispatcherType;
import java.util.EnumSet;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class FilterConfig { // 필터 설정을 위한 구성 클래스, Service Worker 관련 필터 설정을 담당

    // Service Worker 헤더 필터, FCM 푸시 알림을 위한 Service Worker 설정에 필요
    private final OncePerRequestFilter serviceWorkerHeaderFilter;

    // 생성자를 통한 Service Worker 헤더 필터 주입
    // @param serviceWorkerHeaderFilter Service Worker 관련 헤더를 처리하는 필터
    public FilterConfig(OncePerRequestFilter serviceWorkerHeaderFilter) {
        this.serviceWorkerHeaderFilter = serviceWorkerHeaderFilter;
    }

    @Bean // Service Worker 필터 등록을 위한 Bean 설정
    public FilterRegistrationBean<OncePerRequestFilter> serviceWorkerFilterRegistration() {
        FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();

        // Service Worker 헤더 필터 설정
        registration.setFilter(serviceWorkerHeaderFilter);

        // Service Worker 스크립트 파일 경로에만 필터 적용
        registration.addUrlPatterns("/firebase-messaging-sw.js");

        // 요청 타입이 REQUEST인 경우에만 필터 적용 (필터 중복 실행 방지)
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));

        // 필터 식별을 위한 이름 설정
        registration.setName("serviceWorkerHeaderFilter");

        // 필터 실행 순서 설정 (가장 마지막에 실행)
        registration.setOrder(Integer.MAX_VALUE);

        return registration;
    }
}
