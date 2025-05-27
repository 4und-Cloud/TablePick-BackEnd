package com.goorm.tablepick.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationTypes {

    // 알림 타입 상수 정의
    public static final String RESERVATION_COMPLETED = "RESERVATION_COMPLETED";
    public static final String RESERVATION_1DAY_BEFORE = "RESERVATION_1DAY_BEFORE";
    public static final String RESERVATION_3HOURS_BEFORE = "RESERVATION_3HOURS_BEFORE";
    public static final String RESERVATION_1HOUR_BEFORE = "RESERVATION_1HOUR_BEFORE";
    public static final String RESERVATION_3HOURS_AFTER = "RESERVATION_3HOURS_AFTER";
    public static final String REGISTER_COMPLETED = "REGISTER_COMPLETED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(nullable = true)
    private String url;

    // 플레이스홀더를 실제 값으로 치환하는 메서드
    public String getFormattedBody(Map<String, String> parameters) {
        String formattedBody = this.body;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            formattedBody = formattedBody.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return formattedBody;
    }

    // URL의 플레이스홀더를 실제 값으로 치환하는 메서드
    public String getFormattedUrl(Map<String, String> parameters) {
        String formattedUrl = this.url;
        if (formattedUrl == null) {
            return "";
        }

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            formattedUrl = formattedUrl.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return formattedUrl;
    }
}
