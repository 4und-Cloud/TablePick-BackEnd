package com.goorm.tablepick.domain.reservation.config;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@RequiredArgsConstructor
@Component
public class QueryCountInterceptor implements HandlerInterceptor {

    public static final String UNKNOWN_PATH = "UNKNOWN_PATH";

    private final MeterRegistry meterRegistry;

    /**
     * 컨트롤러 실행 전: RequestContext 생성 후 ThreadLocal 에 등록
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. HTTP 메서드 추출
        String httpMethod = request.getMethod();

        // 2. BEST_MATCHING_PATTERN_ATTRIBUTE로부터 path 추출
        String bestMatchPath = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (bestMatchPath == null) {
            bestMatchPath = UNKNOWN_PATH;
        }

        // 3. ThreadLocal 에 저장
        RequestContext ctx = RequestContext.builder()
                .httpMethod(httpMethod)
                .bestMatchPath(bestMatchPath)
                .build();

        RequestContextHolder.initContext(ctx);

        return true;
    }

    /**
     * 요청 처리 완료 시점: 누적된 쿼리 횟수를 꺼내어 MeterRegistry 에 기록하고 ThreadLocal 정리
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        RequestContext ctx = RequestContextHolder.getContext();

        // 1. 쿼리 횟수를 MeterRegistry 에 기록
        if (ctx != null) {
            Map<QueryType, Integer> queryCountByType = ctx.getQueryCountByType();
            queryCountByType.forEach((queryType, count) -> increment(ctx, queryType, count));
        }

        // 2. ThreadLocal 해제
        RequestContextHolder.clear();
    }

    private void increment(RequestContext ctx, QueryType queryType, Integer count) {
        DistributionSummary summary = DistributionSummary.builder("app.query.per_request")
                .description("Number of SQL queries per request")
                .tag("path", ctx.getBestMatchPath())
                .tag("http_method", ctx.getHttpMethod())
                .tag("query_type", queryType.name())
                .publishPercentiles(0.5, 0.95)
                .register(meterRegistry);

        summary.record(count);
    }
}
