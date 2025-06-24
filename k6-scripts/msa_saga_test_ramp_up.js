import http from 'k6/http'; // HTTP 요청을 보내는 기능 가져오기
import { check, sleep } from 'k6'; // 응답 검증 및 실행 일시 정지 기능 가져오기
import {SharedArray} from 'k6/data'; // 여러 가상 사용자(VU)가 데이터를 공유하는 기능 가져오기

//const BASE_URL = 'http://localhost:8080';
const BASE_URL = 'http://172.16.24.77:8080';

const userIds = new SharedArray('userIds', function () {
    return Array.from({length: 1000}, (_, i) => i + 1);
});

export let options = {
    // 임계값(Thresholds): 성능 기대를 정의하고 테스트 실패 조건을 설정합니다.
    thresholds: {
        'http_req_duration{status:200}': ['p(95)<2000'],
        'http_req_failed': ['rate<0.1'],
        'http_reqs': ['count>10000'], // 총 요청 수 목표는 시나리오 길이에 따라 조정 필요
        'checks': ['rate>0.5'],
        'http_req_failed{error_type:connection_reset_by_peer}': ['rate<0.01'],
        'http_req_failed{error_type:서버_오류}': ['rate<0.01'],
        'http_req_failed{error_type:클라이언트_오류}': ['rate<0.01'],
        'http_req_failed{error_type:기타_네트워크_오류}': ['rate<0.01'],
    },

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

export default function () {
    // SAGA 패턴에서는 예약 도메인이 임시 예약 생성 및 Kafka 이벤트 발행 후 즉시 응답하므로,
    // 이 엔드포인트는 결제 도메인으로 리다이렉션 URL 등을 받기 위한 동기 호출이 아닙니다.
    // 임시 예약이 생성되고 Kafka 이벤트가 발행되는 시점까지를 측정합니다.

    const userId = userIds[(__VU - 1) % userIds.length]; // 고유 userId
    // (임시 예약 생성 및 Kafka 이벤트 발행)
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

    let res = http.post(`${BASE_URL}/api/reservations/test/v2/${userId}`, payload, params);

    // --- 더 나은 진단을 위한 세분화된 체크 및 태그 추가 ---
    let isStatus200 = res.status === 200;
    let isNetworkError = res.status === 0 && res.error;
    let isConnectionReset = res.error && res.error.includes('connection reset by peer');
    let isServerError = res.status >= 500 && res.status < 600;
    let isClientError = res.status >= 400 && res.status < 500;

    if (!isStatus200) {
        if (isConnectionReset) {
            res.tags['error_type'] = 'connection_reset_by_peer';
        } else if (isServerError) {
            res.tags['error_type'] = '서버_오류';
        } else if (isClientError) {
            res.tags['error_type'] = '클라이언트_오류';
        } else if (isNetworkError) {
            res.tags['error_type'] = '기타_네트워크_오류';
        } else {
            res.tags['error_type'] = '알수없음_상태코드_실패';
        }
    }

    check(res, {
        'reservation success': (r) => r.status === 200,
    });

    sleep(0.1);
}