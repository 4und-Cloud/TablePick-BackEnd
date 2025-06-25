import http from 'k6/http'; // HTTP 요청을 보내는 기능 가져오기
import { check, sleep } from 'k6'; // 응답 검증 및 실행 일시 정지 기능 가져오기
import { SharedArray } from 'k6/data'; // 여러 가상 사용자(VU)가 데이터를 공유하는 기능 가져오기

//const BASE_URL = 'http://localhost:8080';
const BASE_URL = 'http://172.16.24.77:8080';

const memberIds = new SharedArray('memberIds', function () {
    return Array.from({ length: 1000 }, (_, i) => i + 1);
});

export let options = {

    // Scenarios: K6 테스트의 핵심 실행 방식을 정의합니다.
    scenarios: {
        // 'ramping_http_load': 시나리오의 고유 이름 (Grafana에서 scenario 태그로 사용됨)
        ramping_http_load: {
            executor: 'ramping-vus', // 점진적으로 VU를 증가시키는 실행기 사용
            startVUs: 0,             // 시작 시 가상 사용자 수
            stages: [
                { duration: '10s', target: 50 },  // 1분 동안 0 -> 500 VUs로 증가
                { duration: '30s', target: 100 }, // 다음 3분 동안 500 -> 1000 VUs로 증가
                { duration: '20s', target: 100 }, // 2분 동안 1000 VUs 유지 (고정 부하 구간)
                { duration: '10s', target: 0 },    // 마지막 1분 동안 1000 -> 0 VUs로 감소 (정리)
            ],
            // 총 테스트 시간: 30s + 1m + 30s + 30s = 2분 30초
            gracefulStop: '10s', // 테스트 종료 후 추가 대기 시간
            tags: { test_type: '점진적_HTTP_부하' }, // 이 시나리오의 모든 지표에 추가될 태그 (Grafana 필터링에 유용)
            exec: 'default', // 실행할 함수. 이 스크립트에서는 'default' 함수를 사용합니다.
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