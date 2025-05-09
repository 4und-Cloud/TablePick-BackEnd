package com.goorm.tablepick.domain.board.dto.request;

import lombok.Getter;
import java.util.List;

@Getter
public class BoardFilterRequest {
    private List<String> tags; // 예: ["조용해요", "가성비가 좋아요"]
}
