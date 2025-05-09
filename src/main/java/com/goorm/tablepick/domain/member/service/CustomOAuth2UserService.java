package com.goorm.tablepick.domain.member.service;

import com.goorm.tablepick.domain.member.dto.GoogleInfo;
import com.goorm.tablepick.domain.member.dto.KakaoInfo;
import com.goorm.tablepick.domain.member.dto.OAuthInfo;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import java.time.format.DateTimeFormatter;
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
        Member member = oAuthInfo.toEntity();
        memberRepository.findByEmail(member.getEmail())
                .orElseGet(() -> memberRepository.save(member));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "email", member.getEmail(),
                        "id", member.getProviderId()
                ),
                "email"
        );
    }

    private OAuthInfo createOAuthInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleInfo((String) attributes.get("name"),
                    (String) attributes.get("picture"),
                    (String) attributes.get("email"),
                    (String) attributes.get("sub"));
            case "kakao" -> {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakaoAccount");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                String phoneNumber = (String) kakaoAccount.get("phone_number");

                yield new KakaoInfo((String) profile.get("nickname"),
                        String.valueOf(attributes.get("id")),
                        (String) profile.get("profile_image_url"),
                        (String) kakaoAccount.get("birthyear") + (String) kakaoAccount.get("birthday"),
                        phoneNumber.replace("-", "").replace("+82 ", "0"),
                        (String) kakaoAccount.get("gender"),
                        (String) kakaoAccount.get("email")
                );
            }
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }
}

