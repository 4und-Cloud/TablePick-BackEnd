package com.goorm.tablepick.domain.member.service;

import com.goorm.tablepick.domain.member.dto.GoogleInfo;
import com.goorm.tablepick.domain.member.dto.KakaoInfo;
import com.goorm.tablepick.domain.member.dto.OAuthInfo;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User delegateUser = new DefaultOAuth2UserService().loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" or "kakao"
        Map<String, Object> attributes = delegateUser.getAttributes();

        OAuthInfo oAuthInfo = createOAuthInfo(registrationId, attributes); // provider에서 받아온 사용자 정보
        memberRepository.findByEmail(oAuthInfo.getEmail())
                .orElseGet(() -> memberRepository.save(oAuthInfo.toEntity()));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "email", oAuthInfo.getEmail(),
                        "id", oAuthInfo.getProviderId()
                ),
                "email"
        );
    }

    private OAuthInfo createOAuthInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleInfo(attributes);
            case "kakao" -> new KakaoInfo(attributes, (Map<String, Object>) attributes.get("kakao_account"));
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }
}