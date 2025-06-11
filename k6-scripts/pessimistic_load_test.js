import http from 'k6/http';
import {check, sleep} from 'k6';
import {SharedArray} from 'k6/data';

const BASE_URL = 'http://localhost:8080';
const userIds = new SharedArray('userIds', function () {
    return Array.from({length: 1000}, (_, i) => i + 1);
});

const cookies = {
    access_token: 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwiZW1haWwiOiJldW5zb2wwMzdAbmF2ZXIuY29tIiwiaWF0IjoxNzQ5NDA3MTgyLCJleHAiOjE3NDk0OTM1ODJ9.wKIb5jg0UFB27k1q5j32u5QePaUR7G20zHuA7_l9jj0B7w-dEr71hOSSORcUWk0AzNHMo7te6TkGczb3dWt3RQ',
    refresh_token: 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwiZW1haWwiOiJldW5zb2wwMzdAbmF2ZXIuY29tIiwiaWF0IjoxNzQ5NDA3MTgyLCJleHAiOjE3NTAwMTE5ODJ9.ptkRI_coXkvzipLGzMfBr88Uxav1_s3h6yrUMe7dQKkDFkc-M0EecGqFOr8UsKqGP5e1e26OIdILtYEvPmpj1Q'
};

let userIndex = 0;

export const options = {
    scenarios: {
        reservation_scenario: {
            executor: 'per-vu-iterations',
            vus: 1000, // 1000명 동시에
            iterations: 1, // 1회 실행
            maxDuration: '30s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 높은 부하 고려
    },
};

export default function () {
    const userId = userIds[userIndex % userIds.length];
    userIndex++;

    const payload = JSON.stringify({
        restaurantId: 72,
        reservationDate: '2025-06-09',
        reservationTime: '12:00:00',
        partySize: 1,
    });

    const jar = http.cookieJar();
    jar.set(BASE_URL, 'access_token', cookies.access_token);
    jar.set(BASE_URL, 'refresh_token', cookies.refresh_token);

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.post(`${BASE_URL}/api/reservations/pessimistic`, payload, params);
    check(res, {
        'reservation success': (r) => r.json()?.status === 'success',
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.01); // 높은 부하 시뮬레이션, 짧은 대기
}