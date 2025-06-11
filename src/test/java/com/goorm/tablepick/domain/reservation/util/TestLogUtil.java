package com.goorm.tablepick.domain.reservation.util;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestLogUtil {

    public static void setUpStart() {
        logWithDelimiter("초기화 시작");
    }

    public static void setUpEnd() {
        logWithDelimiter("초기화 종료");
    }

    public static void assertStart() {
        logWithDelimiter("테스트 검증 시작");
    }

    public static void assertEnd() {
        logWithDelimiter("테스트 검증 종료");
    }

    public static void CleanStart() {
        logWithDelimiter("테스트 정리 시작");
    }

    public static void CleanEnd() {
        logWithDelimiter("테스트 정리 종료");
    }

    public static void logWithDelimiter(String msg) {
        System.out.println("###################################  " + msg + "  ###################################");
    }
}