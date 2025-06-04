package com.goorm.tablepick.domain.reservation.service;

import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ReservationSlotPersister {

    private final ReservationSlotRepository slotRepository;

    @Transactional
    public void persistSlots(List<ReservationSlot> slots) {
        for (ReservationSlot slot : slots) {
            // 중복 체크
            if (slotRepository.existsByRestaurantAndDateAndTime(slot.getRestaurant(), slot.getDate(), slot.getTime())) {
                continue; // 이미 존재하면 건너뜀
            }
            slotRepository.save(slot);
        }
    }
}