import http from 'k6/http';
import { check } from 'k6';

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

const BASE_URL = 'http://3.36.121.96:8080/api/restaurants/search/v2';
const QUERY = '?keyword=%EC%8B%9D%EB%8B%B9&tagIds=2&tagIds=3&sort=boardCount&onlyOperating=true&radiusKm=3'
    + '&lat=127.025062823&lng=37.506590029&minPrice=1000&maxPrice=200000';

export function warmup() {
    const res = http.get(`${BASE_URL}${QUERY}`);
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}

export function loadTest() {
    const res = http.get(`${BASE_URL}${QUERY}`);
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}