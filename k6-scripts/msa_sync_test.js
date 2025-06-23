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

    const userId = userIds[(__VU - 1) % userIds.length]; // 고유 userId// MSA 예약 도메인의 동기 호출 엔드포인트 (예시)

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

    let res = http.post(`${BASE_URL}/api/reservations/test/v2/${userId}`, payload, params);

    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(0.1);
}