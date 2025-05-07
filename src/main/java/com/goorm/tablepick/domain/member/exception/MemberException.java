package com.goorm.tablepick.domain.member.exception;

import com.goorm.tablepick.global.exception.CustomException;
import com.goorm.tablepick.global.exception.ErrorCode;

public class MemberException extends CustomException {
    protected MemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}
