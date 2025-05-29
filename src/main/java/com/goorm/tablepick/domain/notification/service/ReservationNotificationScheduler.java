package com.goorm.tablepick.domain.notification.service;

import com.goorm.tablepick.domain.reservation.entity.Reservation;

public interface ReservationNotificationScheduler {
    void scheduleNotificationsDaily();

    void scheduleReservationNotifications(Reservation reservation);
}
