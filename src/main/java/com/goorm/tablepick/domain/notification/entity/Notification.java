package com.goorm.tablepick.domain.notification.entity;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.reservation.entity.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 알림 정보를 저장하는 엔티티 클래스, FCM을 통해 발송된 알림의 이력을 관리
@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 알림 고유 ID

    @Column(length = 20, nullable = false)
    private String type;  // 알림 유형 (예: 예약확정, 리뷰요청 등)

    @Column(length = 50, nullable = false)
    private String title;  // 알림 제목

    @Column(length = 255, nullable = false)
    private String body;  // 알림 내용

    @Column(length = 100)
    private String url;  // 알림 클릭시 이동할 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;  // 연관된 예약 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 알림 수신 회원

    @Column(nullable = false)
    private LocalDateTime sentAt;  // 알림 발송 시간

    @Column(nullable = false)
    private boolean isRead;  // 알림 읽음 여부

    // 모든 필드를 포함하는 생성자
    public Notification(Long id,
                        String type,
                        String title,
                        String body,
                        String url,
                        Reservation reservation,
                        Member member,
                        LocalDateTime sentAt,
                        boolean isRead) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.body = body;
        this.url = url;
        this.reservation = reservation;
        this.member = member;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    // 알림을 읽음 상태로 변경
    public void markAsRead() {
        this.isRead = true;
    }

    // 알림 URL을 업데이트
    public void updateUrl(String url) {
        this.url = url;
    }
}
