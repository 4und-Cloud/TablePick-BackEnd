import http from 'k6/http';
import {check, sleep} from 'k6';
import {SharedArray} from 'k6/data';

//const BASE_URL = 'http://localhost:8080';
const BASE_URL = 'http://172.16.24.77:8080';

const userIds = new SharedArray('userIds', function () {
    return Array.from({length: 5000}, (_, i) => i + 1);
});

export const options = {
    scenarios: {
        reservation_scenario: {
            executor: 'per-vu-iterations',
            vus: 5000,
            iterations: 1,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000'],
    },
};

export default function () {
    const userId = userIds[(__VU - 1) % userIds.length]; // 고유 userId

    const payload = JSON.stringify({
        restaurantId: 1,
        partySize: 1,
        reservationDate: '2025-07-09',
        reservationTime: '12:00',
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
            // 필요 시: 'Authorization': 'Bearer your-token-here',
        },
        timeout: '10s',
    };

    const res = http.post(`${BASE_URL}/api/reservations/test/${userId}`, payload, params);
    console.log(`User ${userId} - Status: ${res.status}, Body: ${res.body || 'No body'}, Error: ${res.error || 'No error'}, Error Code: ${res.error_code || 'No error code'}`);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'no conflict': (r) => r.status !== 409,
        'response exists': (r) => r.status === 200 && !r.body.includes('Login with OAuth 2.0'),
        'not exceed limit': (r) => r.status !== 400 || !r.body.includes('EXCEED_RESERVATION_LIMIT'),
    });

    sleep(0.1);
}