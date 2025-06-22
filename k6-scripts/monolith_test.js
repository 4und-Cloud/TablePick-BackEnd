import http from 'k6/http';
import { check, sleep } from 'k6';
import {SharedArray} from 'k6/data';

const BASE_URL = 'http://localhost:8080';

const memberIds = new SharedArray('memberIds', function () {
    return Array.from({length: 1000}, (_, i) => i + 1);
});

export let options = {
    vus: 1000,  // Virtual Users: 동시 사용자 수
    duration: '30s', // 테스트 지속 시간
    // thresholds: { // 성능 목표 설정 (예시)
    //     http_req_duration: ['p(95)<500'], // 95% 요청이 500ms 미만
    //     http_req_failed: ['rate<0.01'], // 실패율 1% 미만
    // },
};

export default function () {
    const memberId = memberIds[(__VU - 1) % memberIds.length]; // 고유 userId

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

    let res = http.post(`${BASE_URL}/api/reservations/test/v0/${memberId}`, payload, params);

    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(0.1); // 각 요청 사이에 짧은 지연
}