// import http from 'k6/http';
// import {check, sleep} from 'k6';
// import {SharedArray} from 'k6/data';
//
// const BASE_URL = 'http://localhost:8080';
// const userIds = new SharedArray('userIds', function () {
//     return Array.from({length: 1000}, (_, i) => i + 1);
// });
//
// const cookies = {
//     access_token: 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJldW5zb2wwMzdAbmF2ZXIuY29tIiwiaWF0IjoxNzQ5NDEzNjMxLCJleHAiOjE3NDk1MDAwMzF9.upFHdYaQ_eAAgbSr0igU4TpmjtAoSptjEUslr4HmW11-BmoYZ0R3GZxtErB-5BNW-O41qudtZsbfLjzU4d2ylg',
//     refresh_token: 'eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiZW1haWwiOiJldW5zb2wwMzdAbmF2ZXIuY29tIiwiaWF0IjoxNzQ5NDEzNjMxLCJleHAiOjE3NTAwMTg0MzF9.V43WNnVwu7ICb4EPeDiG5HJ4G2MZeMVo0zq7IQDMj3NvXmS8Y8ALOdYQ_gUtldvhWv2jjb1hujvv8N_1xqme2g'
// };
//
// let userIndex = 0;
//
// export const options = {
//     scenarios: {
//         reservation_scenario: {
//             executor: 'per-vu-iterations',
//             vus: 100,
//             iterations: 1,
//             maxDuration: '30s',
//         },
//     },
//     thresholds: {
//         http_req_duration: ['p(95)<1000'],
//     },
// };
//
// export default function () {
//     const userId = userIds[userIndex % userIds.length];
//     userIndex++;
//
//     const payload = JSON.stringify({
//         restaurantId: 72, // BigInt 대신 Number
//         partySize: 1,    // BigInt 대신 Number
//         reservationDate: '2025-06-09',
//         reservationTime: '12:00'
//     });
//
//     const jar = http.cookieJar();
//     jar.set(BASE_URL, 'access_token', cookies.access_token);
//     jar.set(BASE_URL, 'refresh_token', cookies.refresh_token);
//
//     const params = {
//         headers: {
//             'Content-Type': 'application/json',
//             'Authorization': `Bearer ${cookies.access_token}`,
//         },
//     };
//
//     const res = http.post(BASE_URL + '/api/reservations/optimistic', payload, params);
//     console.log(`User ${userId} - Status: ${res.status}, Body: ${res.body}`);
//     check(res, {
//         'status is 200': (r) => r.status === 200,
//         'no conflict': (r) => r.status !== 409,
//         'reservation success': (r) => r.status === 200 && r.json()?.status === 'success',
//         'response exists': (r) => r.body !== null,
//     });
//
//     sleep(0.01);
// }

import http from 'k6/http';
import {check, sleep} from 'k6';
import {SharedArray} from 'k6/data';

//const BASE_URL = 'http://localhost:8080';
const BASE_URL = 'http://172.16.24.77:8080';

const userIds = new SharedArray('userIds', function () {
    return Array.from({length: 1000}, (_, i) => i + 1);
});


let userIndex = 0;

export const options = {
    vus: 1000,
    duration: '30s',
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

    const res = http.post(`${BASE_URL}/api/reservations/test/v0/optimistic/${userId}`, payload, params);
    check(res, {
        'reservation success': (r) => r.json()?.status === 'success',
        'status is 200': (r) => r.status === 200,
    });

    sleep(0.1);
}