package com.goorm.tablepick.domain.reservation.entity;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long partySize;

    @Setter
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_slot_id", nullable = false)
    private ReservationSlot reservationSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private String paymentId; // 결제 ID, 8082 포트에서 관리되는 Payment와 연결
    private String paymentStatus; // PENDING, COMPLETED, FAILED, CANCELLED

    @Column
    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Reservation(Long partySize,
                       ReservationStatus reservationStatus,
                       Member member,
                       ReservationSlot reservationSlot,
                       Restaurant restaurant,
                       String paymentId,
                       String paymentStatus,
                       LocalDateTime createdAt) {
        this.partySize = partySize;
        this.reservationStatus = reservationStatus;
        this.member = member;
        this.reservationSlot = reservationSlot;
        this.restaurant = restaurant;
        this.paymentId = paymentId;
        this.paymentStatus = paymentStatus;
        this.createdAt = createdAt;
    }

    /**
     * 예약 시간을 반환합니다.
     *
     * @return 예약 시간
     */
    public LocalDateTime getReservationDateTime() {
        return this.reservationSlot != null ? this.reservationSlot.getDateTime() : null;
    }

    /**
     * 레스토랑 이름을 반환합니다.
     *
     * @return 레스토랑 이름
     */
    public String getRestaurantName() {
        return this.restaurant != null ? this.restaurant.getName() : null;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
}
