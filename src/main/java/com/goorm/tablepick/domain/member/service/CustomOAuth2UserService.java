package com.goorm.tablepick.domain.member.service;

import com.goorm.tablepick.domain.member.dto.GoogleInfo;
import com.goorm.tablepick.domain.member.dto.KakaoInfo;
import com.goorm.tablepick.domain.member.dto.OAuthInfo;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import java.time.LocalDate;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User delegateUser = new DefaultOAuth2UserService().loadUser(userRequest);
        // "google" or "kakao" 구분
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = delegateUser.getAttributes();
        // provider에서 받아온 사용자 정보
        OAuthInfo oAuthInfo = createOAuthInfo(registrationId, attributes);
        //사용자 정보를 멤버로 변환
        Member member = oAuthInfo.toEntity();
        //멤버가 원래 가입이 되어있으면 로그인 없으면 회원가입
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
            case "google" -> createGoogleInfo(attributes);
            case "kakao" -> createKakaoInfo(attributes);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }

    private GoogleInfo createGoogleInfo(Map<String, Object> attributes) {
        return new GoogleInfo(
                (String) attributes.get("name"),
                (String) attributes.get("picture"),
                (String) attributes.get("email"),
                (String) attributes.get("sub")
        );
    }

    private KakaoInfo createKakaoInfo(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String phoneNumber = (String) kakaoAccount.get("phone_number");
        //반환
        return new KakaoInfo((String) profile.get("nickname"),
                String.valueOf(attributes.get("id")),
                (String) profile.get("profile_image_url"),
                LocalDate.parse((String) kakaoAccount.get("birthyear") + (String) kakaoAccount.get("birthday"),
                        formatter),
                phoneNumber.replace("-", "").replace("+82 ", "0"),
                (String) kakaoAccount.get("gender"),
                (String) kakaoAccount.get("email")
        );
    }
}

