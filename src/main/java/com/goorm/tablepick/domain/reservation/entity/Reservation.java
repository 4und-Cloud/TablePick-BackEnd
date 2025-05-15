package com.goorm.tablepick.domain.reservation.entity;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.notification.entity.Notification;
import com.goorm.tablepick.domain.payment.entity.Payment;
import com.goorm.tablepick.domain.reservation.enums.ReservationStatus;
import com.goorm.tablepick.domain.restaurant.entity.Restaurant;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL)
    private Payment payment;

    @Builder
    public Reservation(Long partySize,
                       ReservationStatus reservationStatus,
                       Member member,
                       ReservationSlot reservationSlot,
                       Restaurant restaurant) {
        this.partySize = partySize;
        this.reservationStatus = reservationStatus;
        this.member = member;
        this.reservationSlot = reservationSlot;
        this.restaurant = restaurant;
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
}
