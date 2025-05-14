package com.goorm.tablepick.domain.notification.entity;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_type_id")
    private NotificationType notificationType;

    private String status;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
}
