package com.goorm.tablepick.domain.notification.constant;

/**
 * 알림 메시지 내용을 정의하는 상수 클래스
 * 각 알림 유형별 실제 전송될 메시지 텍스트를 관리
 */
public class NotificationMessage {

    /**
     * 알림 제목 상수
     */
    public static final String NOTIFICATION_TITLE = "TablePick";

    /**
     * 회원가입 환영 메시지
     * 신규 회원 가입 시 발송
     */
    public static final String WELCOME_MESSAGE = "회원이 되신 것을 환영합니다!";

    /**
     * 예약 완료 메시지
     * 식당 예약이 성공적으로 완료되었을 때 발송
     */
    public static final String RESERVATION_CONFIRMATION_MESSAGE = "예약이 완료되었습니다!";

    /**
     * 예약 1일 전 알림 메시지
     * 예약일 하루 전에 자동 발송
     */
    public static final String ONE_DAY_BEFORE_MESSAGE = "예약날까지 하루 남았습니다!";

    /**
     * 예약 3시간 전 알림 메시지
     * 예약 시간 3시간 전에 자동 발송
     */
    public static final String THREE_HOURS_BEFORE_MESSAGE = "예약시간이 3시간 남았습니다!";

    /**
     * 예약 1시간 전 알림 메시지
     * 예약 시간 1시간 전에 자동 발송
     */
    public static final String ONE_HOUR_BEFORE_MESSAGE = "예약시간이 1시간 남았습니다!";

    /**
     * 리뷰 요청 메시지
     * 예약 시간 3시간 후에 자동 발송
     */
    public static final String REVIEW_REQUEST_MESSAGE = "맛있게 드셨나요? 식당 리뷰 부탁드립니다!";

    /**
     * 유틸리티 클래스의 인스턴스화 방지를 위한 private 생성자
     *
     * @throws AssertionError 이 클래스의 인스턴스화를 시도할 경우 발생
     */
    private NotificationMessage() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }
}
