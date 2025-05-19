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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true)
    private String type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String body;

    @Column(nullable = true)
    private String url;

    public String getFormattedBody(Map<String, String> parameters) {
        String formattedBody = this.body;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            formattedBody = formattedBody.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return formattedBody;
    }

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
