package com.goorm.tablepick.domain.reservation.monitoring;

public class BatchContextHolder {

    // 각 스레드마다 독립적인 RequestContext 저장
    private static final ThreadLocal<BatchContext> context = new ThreadLocal<>();

    public static void initContext(BatchContext ctx) {
        // 이전 컨텍스트가 있다면 제거하고 새로 설정
        context.remove();
        context.set(ctx);
    }

    public static BatchContext getContext() {
        return context.get();
    }

    public static void clear() {
        // 메모리 누수 방지
        context.remove();
    }
}