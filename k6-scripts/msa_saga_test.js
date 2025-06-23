import http from 'k6/http';
import { check, sleep } from 'k6';
import {SharedArray} from 'k6/data';

//const BASE_URL = 'http://localhost:8080';
const BASE_URL = 'http://172.16.24.77:8080';

const userIds = new SharedArray('userIds', function () {
    return Array.from({length: 1000}, (_, i) => i + 1);
});

export let options = {
    vus: 1000,
    duration: '30s',
};

export default function () {
    // SAGA 패턴에서는 예약 도메인이 임시 예약 생성 및 Kafka 이벤트 발행 후 즉시 응답하므로,
    // 이 엔드포인트는 결제 도메인으로 리다이렉션 URL 등을 받기 위한 동기 호출이 아닙니다.
    // 임시 예약이 생성되고 Kafka 이벤트가 발행되는 시점까지를 측정합니다.

    const userId = userIds[(__VU - 1) % userIds.length]; // 고유 userId
    // (임시 예약 생성 및 Kafka 이벤트 발행)
    const payload = JSON.stringify({
        restaurantId: 1,
        partySize: 1,
        reservationDate: '2025-07-09',
        reservationTime: '12:00',
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    let res = http.post(`${BASE_URL}/api/reservations/test/v1/${userId}`, payload, params);

    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(0.1);
}