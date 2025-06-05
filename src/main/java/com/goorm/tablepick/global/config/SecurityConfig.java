package com.goorm.tablepick.global.config;

import com.goorm.tablepick.domain.member.service.CustomOAuth2UserService;
import com.goorm.tablepick.global.filter.JwtTokenFilter;
import com.goorm.tablepick.global.jwt.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final JwtTokenFilter jwtTokenFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/dev/**", "/api/notifications/schedule/**",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/auth/**", "/oauth2/**", "/swagger-ui.html/**",
                                "/api/restaurants/all", "/api/restaurants/{id}",
                                "/api/boards/main", "/api/tags", "/api/restaurants/search",
                                "/api/restaurants/list", "/api/reservation/available-times", "/api/board-tags/",
                                "/api/boards/list", "api/boards", "/api/boards/{boardId}",
                                "/api/boards/restaurant/{restaurantId}", "/images/**", "/api/restaurants/v1/search"
                        ).permitAll()

                        // 🔧 권한 검사 조건
                        .requestMatchers("/api/**").authenticated() // 테스트 목적이라면 이렇게
                        //.requestMatchers("/api/**").hasAuthority("ROLE_USER") // 실제 권한 검사 시 이렇게

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(successHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/members/logout")  // 로그아웃 URL 변경
                        .logoutSuccessUrl("/")  // 로그아웃 성공 후 이동할 URL
                        .invalidateHttpSession(true)  // 세션 무효화
                        .deleteCookies("JSESSIONID", "access_token", "refresh_token")
                );

        return http.build();
    }
}
