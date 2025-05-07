package com.goorm.tablepick.domain.member.dto;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import com.goorm.tablepick.domain.member.enums.Gender;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KakaoInfo implements OAuthInfo {
    private final Map<String, Object> attributes;
    @Getter
    private final Map<String, Object> kakaoAccount;

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = getKakaoAccount();
        return (String) kakaoAccount.get("email");
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getProvider() {
        return "kakao";
    }

    @Override
    public Member toEntity() {
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String phoneNumber = (String) kakaoAccount.get("phone_number");

        return Member.builder()
                .email((String) kakaoAccount.get("email"))
                .nickname((String) profile.get("nickname"))
                .gender(Gender.from((String) kakaoAccount.get("gender")))
                .phoneNumber(phoneNumber.replace("-", "").replace("+82 ", "0"))
                .birthdate(
                        LocalDate.parse((String) kakaoAccount.get("birthyear") + (String) kakaoAccount.get("birthday"),
                                formatter))
                .profileImage((String) profile.get("profile_image_url"))
                .roles(AccountRole.USER)
                .isMemberDeleted(false)
                .provider(getProvider())
                .providerId(getProviderId())
                .build();
    }
}
