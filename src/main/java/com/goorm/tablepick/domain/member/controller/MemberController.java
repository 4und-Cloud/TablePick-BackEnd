package com.goorm.tablepick.domain.member.controller;

import com.goorm.tablepick.domain.member.dto.MemberResponseDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import com.goorm.tablepick.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    @Operation(summary = "로그인한 사용자 정보 조회", description = "회원가입 후 추가적인 사용자 정보를 받습니다.")
    public ResponseEntity<MemberResponseDto> getMemberAfterRegistration(
            @AuthenticationPrincipal UserDetails userDetails) {
        MemberResponseDto dto = memberService.getMemberInfo("test@example.com");
        return ResponseEntity.ok(dto);
    }

    @PatchMapping
    @Operation(summary = "사용자 정보 수정", description = "닉네임, 전화번호, 성별, 프로필 사진, 프로필 이미지, 사용자 태그 수정 가능합니다.")
    public ResponseEntity<Void> updateMember(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestBody @Valid MemberUpdateRequestDto memberUpdateRequestDto) {
        memberService.updateMemberInfo(userDetails.getUsername(), memberUpdateRequestDto);
        return ResponseEntity.ok().build();
    }

}