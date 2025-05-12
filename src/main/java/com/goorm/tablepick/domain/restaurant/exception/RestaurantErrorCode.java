package com.goorm.tablepick.domain.restaurant.exception;

import com.goorm.tablepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestaurantErrorCode implements ErrorCode {

    NOT_FOUND("식당 정보를 찾을 수 없습니다."),
    NO_OPERATING_HOUR("해당하는 운영 시간을 찾을 수 없습니다"),
    NO_RESTAURANT_CATEGORY("해당하는 카테고리를 찾을 수 없습니다.");

    private final String message;
}