package com.goorm.tablepick.global.security;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    // ✅ 명시적으로 member 반환 메서드 추가 (이미 Lombok @Getter 있어도 명시 권장)
    public Member getMember() {
        return this.member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        AccountRole auth = member.getRoles();

        List<SimpleGrantedAuthority> roles = new ArrayList<>();
        roles.add(new SimpleGrantedAuthority("ROLE_" + auth));

        return roles;
    }

    @Override
    public String getPassword() {
        return null; // 소셜 로그인은 비밀번호 없음
    }

    @Override
    public String getUsername() {
        return member.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return !member.getIsMemberDeleted(); // 탈퇴 여부 체크
    }

    public Long getId() {
        return member.getId();
    }
}
