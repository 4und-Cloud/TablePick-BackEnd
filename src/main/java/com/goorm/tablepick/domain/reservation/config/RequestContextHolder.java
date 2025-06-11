package com.goorm.tablepick.domain.reservation.config;

public class RequestContextHolder {

    // 각 스레드별로 독립된 RequestContext 저장소
    private static final ThreadLocal<RequestContext> CONTEXT = new ThreadLocal<>();

    public static void initContext(RequestContext context) {
        // 이전 컨텍스트가 있다면 제거하고 새로 설정
        CONTEXT.remove();
        CONTEXT.set(context);
    }

    public static RequestContext getContext() {
        return CONTEXT.get();
    }

    public static void clear() {
        // 메모리 누수 방지를 위해 반드시 호출 필요
        CONTEXT.remove();
    }
}