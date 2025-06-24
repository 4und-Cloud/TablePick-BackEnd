import http from 'k6/http'; // HTTP 요청을 보내는 기능 가져오기
import { check, sleep } from 'k6'; // 응답 검증 기능 가져오기
import { SharedArray } from 'k6/data'; // 여러 가상 사용자(VU)가 데이터를 공유하는 기능 가져오기

// 1. 기본 설정
// ===================================

//const BASE_URL = 'http://localhost:8080';
const BASE_URL = 'http://172.16.24.77:8080';

// 고유한 사용자 ID를 미리 생성하여 테스트에 사용합니다.
// VU 수와 동일하게 1000개로 설정하는 것이 좋습니다.
const userIds = new SharedArray('userIds', function () {
    return Array.from({ length: 1000 }, (_, i) => i + 1);
});

// export const options = {
//     vus: 1000,
//     duration: '30s',
// };

export const options = {
    scenarios: {
        single_shot: {
            executor: "per-vu-iterations", // VU당 반복 횟수 지정
            vus: 1000,                     // 가상 사용자 수
            iterations: 1,                 // 각 VU는 한 번만 요청
            maxDuration: '20s',            // 전체 테스트 제한 시간
        },
    },
};


// 2. 가상 사용자(VU)별 실행 함수 (핵심 로직)
// ===================================

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
        },
        timeout: '10s',
    };

    const res = http.post(`${BASE_URL}/api/reservations/test/v0/optimistic/${userId}`, payload, params);

    check(res, {

        'reservation success': (r) => r.status === 200,
    });

    //sleep(0.1);
}