package com.goorm.tablepick.domain.reservation.config;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RequestContext {
    private String httpMethod;
    private String bestMatchPath;
    private final Map<QueryType, Integer> queryCountByType = new HashMap<>();

    @Builder
    public RequestContext(String httpMethod, String bestMatchPath) {
        this.httpMethod = httpMethod;
        this.bestMatchPath = bestMatchPath;
    }

    public void incrementQueryCount(String sql) {
        QueryType queryType = QueryType.from(sql);
        queryCountByType.merge(queryType, 1, Integer::sum);
    }
}