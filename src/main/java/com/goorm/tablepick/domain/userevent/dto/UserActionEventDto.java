package com.goorm.tablepick.domain.userevent.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserActionEventDto implements Serializable {
    private String actionEventType;      // 예: "RESTAURANT_CLICK"
    private Long targetId;         // 클릭 대상 ID
    private Long userId;           // 사용자 ID
    private Long timestamp;        // 클릭 시각
}
