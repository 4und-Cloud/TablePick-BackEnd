import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    scenarios: {
        // 1) 워밍업: VU 1개로 10번 요청
        warmup: {
            executor: 'shared-iterations',
            vus: 1,
            iterations: 10,
            startTime: '0s',
            exec: 'warmup',
        },
        // 2) 메인 부하: VU 100개, 각 VU가 1회씩 (=100개 요청)
        load: {
            executor: 'shared-iterations',
            vus: 100,
            iterations: 100,
            startTime: '5s',
            exec: 'loadTest',
        },
    },
    thresholds: {
        'http_req_duration': ['p(95)<50000'],  // 95% 요청은 50000ms 이내
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://3.36.121.96:8080';

export default function () {
    const params = {
        keyword: '한식',
        tagIds: [2, 3, 4],
        onlyOperating: true,
        sort: 'reservationCount',
        page: 0,
    };

    // 수동으로 쿼리스트링 조립
    let query = `?keyword=${encodeURIComponent(params.keyword)}` +
        `&onlyOperating=${params.onlyOperating}` +
        `&sort=${encodeURIComponent(params.sort)}` +
        `&page=${params.page}`;
    for (const id of params.tagIds) {
        query += `&tagIds=${id}`;
    }

    const url = `${BASE_URL}/api/restaurants/v1/search${query}`;
    const res = http.get(url, { timeout: '60s' });

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}