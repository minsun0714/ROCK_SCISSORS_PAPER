import http from "k6/http";
import { WebSocket } from "k6/websockets";
import { check, sleep } from "k6";

const BASE_URL = "https://rock-scissors-paper.site";
const WS_URL = "wss://rock-scissors-paper.site/ws";

export const options = {
  thresholds: {
    http_req_failed: ["rate<0.01"],

    http_req_duration: [
      "p(95)<300",
      "p(99)<500",
    ],

    iteration_duration: [
      "p(95)<2000",
    ],

    checks: [
      "rate>0.99",
    ],
  },

  scenarios: {
    battle_flow: {
      executor: "ramping-vus",
      stages: [
        { target: 50, duration: "2m" },
        { target: 100, duration: "2m" },
        { target: 200, duration: "2m" },
      ],
      gracefulRampDown: "30s",
    },
  },
};

//
// 1️⃣ 테스트 유저 생성
//
export function setup() {

  const users = [];
  const USER_COUNT = 2000;

  for (let i = 0; i < USER_COUNT; i++) {

    const res = http.post(
      `${BASE_URL}/auth/loadtest/users?name=user_${i}`
    );

    if (res.status === 200) {
      users.push(res.json());
    }
  }

  const pairs = [];

  for (let i = 0; i < users.length; i += 2) {
    pairs.push({
      requester: users[i],
      opponent: users[i + 1],
    });
  }

  return pairs;
}

//
// 2️⃣ 실제 테스트
//
export default function (pairs) {

  const pair = pairs[__VU % pairs.length];

  const requester = pair.requester;
  const opponent = pair.opponent;

  const reqHeaders = {
    Authorization: `Bearer ${requester.token}`,
    "Content-Type": "application/json",
  };

  const oppHeaders = {
    Authorization: `Bearer ${opponent.token}`,
    "Content-Type": "application/json",
  };

  //
  // 3️⃣ 배틀 신청
  //
  const battleRes = http.post(
    `${BASE_URL}/battles/requests`,
    JSON.stringify({ targetUserId: opponent.userId }),
    { headers: reqHeaders }
  );

  check(battleRes, {
    "battle request ok": (r) => [200, 201].includes(r.status),
  });

  if (![200, 201].includes(battleRes.status)) {
    sleep(0.5);
    return;
  }

  const roomId = battleRes.json().roomId;

  //
  // 4️⃣ 배틀 수락
  //
  const acceptRes = http.patch(
    `${BASE_URL}/battles/requests/${roomId}/accept`,
    null,
    { headers: oppHeaders }
  );

  check(acceptRes, {
    "battle accept ok": (r) => r.status === 200,
  });

  if (acceptRes.status !== 200) {
    sleep(0.5);
    return;
  }

  //
  // 5️⃣ WebSocket 연결
  //
  const requesterSocket = new WebSocket(
    `${WS_URL}?token=${requester.token}&roomId=${roomId}`
  );

  const opponentSocket = new WebSocket(
    `${WS_URL}?token=${opponent.token}&roomId=${roomId}`
  );

  let finished = 0;

  requesterSocket.onopen = () => {};

  requesterSocket.onmessage = (event) => {

    const msg = JSON.parse(event.data);

    if (msg.type === "BATTLE_START") {
      requesterSocket.send(JSON.stringify({
        type: "CHOICE",
        move: "ROCK",
      }));
    }

    if (msg.type === "BATTLE_FINISHED") {
      finished++;
      requesterSocket.close();
    }
  };

  opponentSocket.onmessage = (event) => {

    const msg = JSON.parse(event.data);

    if (msg.type === "BATTLE_START") {
      opponentSocket.send(JSON.stringify({
        type: "CHOICE",
        move: "SCISSORS",
      }));
    }

    if (msg.type === "BATTLE_FINISHED") {
      finished++;
      opponentSocket.close();
    }
  };

  requesterSocket.onerror = () => requesterSocket.close();
  opponentSocket.onerror = () => opponentSocket.close();

  //
  // 6️⃣ 배틀 종료 대기
  //
  let wait = 0;

  while (finished < 2 && wait < 40) {
    sleep(0.5);
    wait++;
  }

  try { requesterSocket.close(); } catch (e) {}
  try { opponentSocket.close(); } catch (e) {}

  //
  // 7️⃣ 전적 조회
  //
  const statsRes = http.get(
    `${BASE_URL}/battles/stats/me`,
    { headers: reqHeaders }
  );

  check(statsRes, {
    "stats ok": (r) => r.status === 200,
  });

  //
  // 8️⃣ 히스토리 조회
  //
  const historyRes = http.get(
    `${BASE_URL}/battles/history/me?page=0&size=20`,
    { headers: reqHeaders }
  );

  check(historyRes, {
    "history ok": (r) => r.status === 200,
  });

  sleep(0.5);
}