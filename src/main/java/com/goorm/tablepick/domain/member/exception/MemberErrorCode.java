package com.goorm.tablepick.domain.member.exception;

import com.goorm.tablepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    NOT_FOUND("사용자 정보를 찾을 수 없습니다."),
    NO_MEMBER_TAG("해당 멤버 태그가 존재하지 않습니다."),
    NO_SUCH_TAG("해당 태그가 존재하지 않습니다.");

    private final String message;

}
