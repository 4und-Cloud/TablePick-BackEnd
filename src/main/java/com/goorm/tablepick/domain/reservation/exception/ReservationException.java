package com.goorm.tablepick.domain.reservation.exception;

import com.goorm.tablepick.global.exception.CustomException;

public class ReservationException extends CustomException {
    public ReservationException(ReservationErrorCode errorCode) {
        super(errorCode);
    }
}