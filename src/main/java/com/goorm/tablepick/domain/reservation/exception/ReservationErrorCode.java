package com.goorm.tablepick.domain.reservation.exception;

import com.goorm.tablepick.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationErrorCode implements ErrorCode {

    NOT_FOUND("예약 정보를 찾을 수 없습니다."),
    NO_OPERATING_HOUR("해당 날짜에 운영 시간이 존재하지 않습니다."),
    NO_RESERVATION_SLOT("해당 예약 가능 시간이 존재하지 않습니다."),
    ALL_SLOTS_BOOKED("모든 예약 시간이 이미 마감되었습니다."),
    EXCEED_RESERVATION_LIMIT("해당 예약 시간이 이미 마감되었습니다."),
    ALREADY_CANCELLED("이미 취소된 예약입니다."),
    UNAUTHORIZED_CANCEL("예약 취소 권한이 없습니다."),
    DUPLICATE_RESERVATION("중복된 예약입니다."),
    INTERNAL_SERVER_ERROR("내부 서버 오류로 인해 예약 실패."),
    PAYMENT_FAILED("결제가 실패 되었습니다"),
    OPTIMISTIC_LOCK_RETRY_EXCEEDED("최대 재시도 횟수를 초과했습니다.");

    private final String message;
}