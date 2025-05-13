package com.goorm.tablepick.domain.notification.controller;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.notification.dto.request.FCMTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Tag(name = "FCM Token API", description = "FCM 토큰 관리 API")
public class FCMTokenController {

    private final MemberRepository memberRepository;

    @PostMapping("/token")
    @Operation(summary = "FCM 토큰 등록", description = "회원의 FCM 토큰을 등록합니다.")
    public ResponseEntity<String> registerToken(@RequestBody FCMTokenRequest request) {
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        member.updateFcmToken(request.getToken());
        memberRepository.save(member);

        return ResponseEntity.ok("FCM 토큰이 등록되었습니다.");
    }

    @DeleteMapping("/token/{memberId}")
    @Operation(summary = "FCM 토큰 삭제", description = "회원의 FCM 토큰을 삭제합니다.")
    public ResponseEntity<String> removeToken(@PathVariable Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        member.removeFcmToken();
        memberRepository.save(member);

        return ResponseEntity.ok("FCM 토큰이 삭제되었습니다.");
    }
}
