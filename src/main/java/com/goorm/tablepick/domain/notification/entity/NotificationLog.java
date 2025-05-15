package com.goorm.tablepick.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // Builder를 위해 필요
@Builder    // 추가
@Table(name = "notification_logs")
public class NotificationLog {
    @Id
    private String key;

    @Column(name = "notification_queue_id")
    private Long notificationQueueId;

    private LocalDateTime sentAt;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Column(name = "error_message")
    private String errorMessage;
}
