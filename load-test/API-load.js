import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = "https://rock-paper-scissors.online";

export const options = {
  thresholds: {
    http_req_duration: ["p(95)<150", "p(99)<200"],
    http_req_failed: ["rate<0.01"],
    checks: ["rate>0.99"]
  },
  scenarios: {
    battle_flow: {
      executor: "ramping-arrival-rate",
      startRate: 10,
      timeUnit: "1s",
      stages: [
        { target: 10, duration: "2m" },
        { target: 50, duration: "2m" },
        { target: 100, duration: "2m" },
        { target: 200, duration: "2m" },
      ],
      preAllocatedVUs: 100,
      maxVUs: 400,
    },
  },
};

// VU별로 유저 쌍 생성 + 토큰 발급
export function setup() {
  const pairs = [];
  const pairCount = 300;

  for (let i = 0; i < pairCount; i++) {
    const res1 = http.post(
      `${BASE_URL}/auth/loadtest/users?name=requester_${i}`
    );
    const res2 = http.post(
      `${BASE_URL}/auth/loadtest/users?name=opponent_${i}`
    );

    if (res1.status === 200 && res2.status === 200) {
      pairs.push({
        requester: res1.json(),
        opponent: res2.json(),
      });
    }
  }

  return { pairs };
}

export default function (data) {
  const pair = data.pairs[__ITER % data.pairs.length];
  const requester = pair.requester;
  const opponent = pair.opponent;

  const requesterHeaders = {
    Authorization: `Bearer ${requester.token}`,
    "Content-Type": "application/json",
  };
  const opponentHeaders = {
    Authorization: `Bearer ${opponent.token}`,
    "Content-Type": "application/json",
  };

  // 1. 유저 검색
  const searchRes = http.get(
    `${BASE_URL}/users/search?keyword=requester_`,
    { headers: requesterHeaders }
  );
  check(searchRes, {
    "search 200": (r) => r.status === 200,
  });

  // 2. 배틀 신청
  const battleRes = http.post(
    `${BASE_URL}/battles/requests`,
    JSON.stringify({ targetUserId: opponent.userId }),
    { headers: requesterHeaders }
  );
  check(battleRes, {
    "battle request ok": (r) => [200,201,409].includes(r.status),
  });

  if (battleRes.status === 200 || battleRes.status === 201) {
    const roomId = battleRes.json().roomId;

    // 3. 배틀 수락
    const acceptRes = http.patch(
    `${BASE_URL}/battles/requests/${roomId}/accept`,
    null,
    { headers: opponentHeaders }
    );
    check(acceptRes, {
    "battle accept ok": (r) => r.status === 200,
    });
  }
  // 4. 전적 통계 조회
  const statRes = http.get(
    `${BASE_URL}/battles/stats/me`,
    { headers: requesterHeaders }
  );
  check(statRes, {
    "stat 200": (r) => r.status === 200,
  });

  // 5. 히스토리 조회
  const historyRes = http.get(
    `${BASE_URL}/battles/history/me?page=0&size=20`,
    { headers: requesterHeaders }
  );
  check(historyRes, {
    "history 200": (r) => r.status === 200,
  });
}
