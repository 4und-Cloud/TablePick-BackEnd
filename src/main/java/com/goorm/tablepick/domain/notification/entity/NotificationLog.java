package com.goorm.tablepick.domain.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "notification_logs")
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_queue_id")
    private Long notificationQueueId;

    private LocalDateTime sentAt;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Column(name = "error_message")
    private String errorMessage;
}
