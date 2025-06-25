import http from 'k6/http'; // HTTP 요청을 보내는 기능 가져오기
import { check, sleep } from 'k6'; // 응답 검증 및 실행 일시 정지 기능 가져오기
import { SharedArray } from 'k6/data'; // 여러 가상 사용자(VU)가 데이터를 공유하는 기능 가져오기

// 1. 기본 설정 (스크립트 상단)
// ===================================

// 예약 서비스의 기본 URL 설정
const BASE_URL = 'http://172.16.24.77:8080';

// 고유한 사용자 ID를 미리 생성하여 테스트에 사용합니다. (1부터 1000까지)
// SharedArray는 모든 VU(Virtual User)가 공유하는 데이터를 효율적으로 관리할 때 사용됩니다.
// 이렇게 하면 각 VU가 서로 다른 사용자 ID를 사용하여 실제 사용자의 행동을 더 잘 모방할 수 있습니다.
const userIds = new SharedArray('userIds', function () {
    return Array.from({ length: 10000 }, (_, i) => i + 1);
});

// 테스트 옵션 설정: k6 테스트의 전반적인 동작 방식을 정의합니다.
export const options = {
    scenarios: {
        single_shot: {
            executor: "per-vu-iterations", // VU당 반복 횟수 지정
            vus: 10000,                     // 가상 사용자 수
            iterations: 1,                 // 각 VU는 한 번만 요청
            maxDuration: '30s',            // 전체 테스트 제한 시간
        },
    },
};

// 2. 가상 사용자(VU)별 실행 함수 (핵심 로직)
// ===================================

// `default` 함수는 각 가상 사용자(VU)가 반복적으로 실행하는 코드 블록입니다.
export default function () {
    // 각 VU는 고유한 userId를 사용하여 요청을 보냅니다.
    // `(__VU - 1) % userIds.length`를 통해 1000개의 userId를 순환하며 사용합니다.
    const userId = userIds[(__VU - 1) % userIds.length];

    // 예약 요청을 위한 JSON 페이로드(요청 본문)를 정의합니다.
    const payload = JSON.stringify({
        restaurantId: 1, // 테스트할 식당 ID
        partySize: 1,    // 파티 인원
        reservationDate: '2025-07-09', // 예약 날짜
        reservationTime: '12:00',      // 예약 시간
    });

    // HTTP 요청에 포함될 헤더를 정의합니다. (여기서는 JSON 타입임을 명시)
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // 실제 HTTP POST 요청을 전송하고 응답을 `res` 변수에 저장합니다.
    // `api/reservations/test/v2/{userId}` 엔드포인트로 요청을 보냅니다.
    let res = http.post(`${BASE_URL}/api/reservations/test/v1/${userId}`, payload, params);



    // `check` 함수를 사용하여 응답의 유효성을 검증합니다.
    // 각 `check`는 성공 또는 실패로 기록되며, 'checks' 메트릭에 반영됩니다.
    check(res, {
        'reservation success': (r) => r.status === 200,
    });

    // sleep(0.1);
}