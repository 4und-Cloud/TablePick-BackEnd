package com.goorm.tablepick.domain.restaurant.exception;

import com.goorm.tablepick.global.exception.CustomException;

public class RestaurantException extends CustomException {
    public RestaurantException(RestaurantErrorCode errorCode) {
        super(errorCode);
    }
}