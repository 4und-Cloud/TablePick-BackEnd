package com.goorm.tablepick.domain.member.exception;

import com.goorm.tablepick.global.exception.CustomException;

public class MemberException extends CustomException {
    public MemberException(MemberErrorCode errorCode) {
        super(errorCode);
    }
}
