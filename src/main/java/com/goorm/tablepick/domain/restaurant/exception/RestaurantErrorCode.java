package com.goorm.tablepick.domain.restaurant.exception;

import com.goorm.tablepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestaurantErrorCode implements ErrorCode {

    NOT_FOUND("식당 정보를 찾을 수 없습니다.");

    private final String message;
}