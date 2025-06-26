import http from 'k6/http'; // HTTP 요청을 보내는 기능 가져오기
import { check, sleep } from 'k6'; // 응답 검증 및 실행 일시 정지 기능 가져오기
import { SharedArray } from 'k6/data'; // 여러 가상 사용자(VU)가 데이터를 공유하는 기능 가져오기

//const BASE_URL = 'http://localhost:8080';
const BASE_URL = 'http://172.16.24.77:8080';

const memberIds = new SharedArray('memberIds', function () {
    return Array.from({ length: 1000 }, (_, i) => i + 1);
});

export const options = {
    scenarios: {
        single_shot: {
            executor: "per-vu-iterations", // VU당 반복 횟수 지정
            vus: 1000,                     // 가상 사용자 수
            iterations: 1,                 // 각 VU는 한 번만 요청
            maxDuration: '30s',            // 전체 테스트 제한 시간
        },
    },
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

    check(res, {
        'reservation success': (r) => r.status === 200,
    });

    sleep(0.1); // 각 요청 사이에 짧은 지연
}