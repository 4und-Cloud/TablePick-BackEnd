package com.goorm.tablepick.domain.notification.entity;

import com.goorm.tablepick.domain.notification.constant.NotificationTypes;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationTypes notificationType;

    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
}
