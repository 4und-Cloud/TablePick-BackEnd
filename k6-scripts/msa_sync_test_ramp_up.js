import http from 'k6/http'; // HTTP 요청을 보내는 기능 가져오기
import { check, sleep } from 'k6'; // 응답 검증 및 실행 일시 정지 기능 가져오기
import { SharedArray } from 'k6/data'; // 여러 가상 사용자(VU)가 데이터를 공유하는 기능 가져오기

// 1. 기본 설정
// ===================================

// 예약 서비스의 기본 URL 설정
const BASE_URL = 'http://172.16.24.77:8080';

// 고유한 사용자 ID를 미리 생성하여 테스트에 사용합니다. (1부터 10000까지, 더 많은 VU를 위해 늘림)
const userIds = new SharedArray('userIds', function () {
    return Array.from({ length: 10000 }, (_, i) => i + 1); // VU가 늘어날 것을 대비하여 userId 풀도 늘림
});

// 테스트 옵션 설정: 점진적 부하 시나리오 정의
export let options = {


    // Scenarios: K6 테스트의 핵심 실행 방식을 정의합니다.
    scenarios: {
        // 'ramping_http_load': 시나리오의 고유 이름 (Grafana에서 scenario 태그로 사용됨)
        ramping_http_load: {
            executor: 'ramping-vus', // 점진적으로 VU를 증가시키는 실행기 사용
            startVUs: 0,             // 시작 시 가상 사용자 수
            stages: [
                { duration: '10s', target: 500 },  // 1분 동안 0 -> 500 VUs로 증가
                { duration: '30s', target: 1000 }, // 다음 3분 동안 500 -> 1000 VUs로 증가
                { duration: '20s', target: 1000 }, // 2분 동안 1000 VUs 유지 (고정 부하 구간)
                { duration: '10s', target: 0 },    // 마지막 1분 동안 1000 -> 0 VUs로 감소 (정리)
            ],
            // 총 테스트 시간: 30s + 1m + 30s + 30s = 2분 30초
            gracefulStop: '10s', // 테스트 종료 후 추가 대기 시간
            tags: { test_type: '점진적_HTTP_부하' }, // 이 시나리오의 모든 지표에 추가될 태그 (Grafana 필터링에 유용)
            exec: 'default', // 실행할 함수. 이 스크립트에서는 'default' 함수를 사용합니다.
        },
    },
};

// 2. 가상 사용자(VU)별 실행 함수 (핵심 로직)
// ===================================

// `default` 함수는 각 가상 사용자(VU)가 반복적으로 실행하는 코드 블록입니다.
// 이 함수는 'ramping_http_load' 시나리오에서 호출됩니다.
export default function () {
    const userId = userIds[(__VU - 1) % userIds.length];

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

    check(res, {
        'reservation success': (r) => r.status === 200,
    });

    // VU 간의 부하를 좀 더 고르게 분산하기 위해 sleep 시간을 유지합니다.
    // sleep 시간을 너무 짧게 하면 K6 자체의 CPU 사용률이 높아질 수 있습니다.
    sleep(0.1);
}
