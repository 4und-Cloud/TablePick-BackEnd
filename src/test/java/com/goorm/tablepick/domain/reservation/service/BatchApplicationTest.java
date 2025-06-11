package com.goorm.tablepick.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.goorm.tablepick.domain.reservation.config.IntegrationTest;
import com.goorm.tablepick.domain.reservation.entity.ReservationSlot;
import com.goorm.tablepick.domain.reservation.repository.ReservationRepository;
import com.goorm.tablepick.domain.reservation.repository.ReservationSlotRepository;
import com.goorm.tablepick.domain.reservation.util.TestLogUtil;
import com.goorm.tablepick.domain.restaurant.repository.RestaurantRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
public class BatchApplicationTest {

    // 실제 삽입 데이터 수 = size(식당 수) * 30일 * 10타임
    //16624
    private final int size = 5024;

    @Autowired
    private BatchApplication batchApplication;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationSlotGenerator reservationSlotGenerator;

    @Autowired
    private RestaurantRepository restaurantRepository;


    @AfterEach
    void cleanUp() {
        TestLogUtil.CleanStart();
        reservationRepository.deleteAllInBatch();
        reservationSlotRepository.deleteAllInBatch();
        assertThat(reservationSlotRepository.count()).isZero();
        TestLogUtil.CleanEnd();
    }

    @Test
    void testBulkInsertWithMonitoring() {
        // when
        List<ReservationSlot> data = reservationSlotGenerator.generateSlotsForTest(size, restaurantRepository);
        batchApplication.bulkInsertWithMonitoring(data);

        // then
        TestLogUtil.assertStart();
        assertThat(reservationSlotRepository.count()).isEqualTo(size * 30 * 10);
        TestLogUtil.assertEnd();
    }

    @Test
    void testBulkDeleteWithMonitoring() {
        // given
        TestLogUtil.setUpStart();
        List<ReservationSlot> data = reservationSlotGenerator.generateSlotsForTest(size, restaurantRepository);
        List<ReservationSlot> slots = reservationSlotRepository.saveAll(data);
        assertThat(slots).hasSize(size * 30 * 10);
        TestLogUtil.setUpEnd();

        // when
        batchApplication.bulkDeleteWithMonitoring();

        // then
        TestLogUtil.assertStart();
        assertThat(reservationSlotRepository.count()).isZero();
        TestLogUtil.assertEnd();
    }

}
