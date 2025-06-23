import http from 'k6/http';
import { check, sleep } from 'k6';
import {SharedArray} from 'k6/data';

//const BASE_URL = 'http://localhost:8080';
const BASE_URL = 'http://172.16.24.77:8080';

const memberIds = new SharedArray('memberIds', function () {
    return Array.from({length: 1000}, (_, i) => i + 1);
});

export let options = {
    vus: 1000,
    duration: '30s',
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

    let res = http.post(`${BASE_URL}/api/reservations/test/${memberId}`, payload, params);

    check(res, { 'status is 200': (r) => r.status === 200 });
    sleep(0.1); // 각 요청 사이에 짧은 지연
}