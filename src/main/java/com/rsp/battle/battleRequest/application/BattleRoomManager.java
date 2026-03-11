package com.rsp.battle.battleRequest.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsp.battle.battleRequest.domain.Move;
import com.rsp.battle.battleRequest.presentation.dto.response.BattleResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;

@Component
@Slf4j
public class BattleRoomManager {

    static class Room {
        Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
        ScheduledFuture<?> timer;
    }

    private final Map<Long, Room> rooms = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final BattleService battleService;

    public BattleRoomManager (BattleService battleService) {
        this.battleService = battleService;
    }

    private final static Integer ROOM_MAX_SIZE = 2;
    private final static Integer WAIT_SECONDS_UNTIL_OPPONENT_ENTER_ROOM = 60 * 3;
    private final static Integer WAIT_SECONDS_UNTIL_OPPONENT_MOVE = 30;

    public void join(WebSocketSession currentSession) {
        Long roomId = (Long) currentSession.getAttributes().get("roomId");
        Long userId = (Long) currentSession.getAttributes().get("userId");

        Room room = rooms.computeIfAbsent(roomId, r -> new Room()); // getOrDefault에서 동시성 이슈로 변경

        // 새로고침 등으로 인한 재접속 고려
        room.sessions.removeIf(s ->
                s.getAttributes().get("userId").equals(userId)
        );

        if (room.sessions.size() >= ROOM_MAX_SIZE
                || !room.sessions.isEmpty() && Objects.requireNonNull(room.timer).isDone()) {
            closeSession(currentSession);
            return;
        }

        room.sessions.add(currentSession);

        if (room.sessions.size() < ROOM_MAX_SIZE) {
            room.timer = setTimer(roomId, () -> {
                broadcast(roomId, "상대방이 5분 동안 입장하지 않아 배틀이 종료됩니다.");
                for (WebSocketSession s : room.sessions) {
                    closeSession(s);
                }
                rooms.remove(roomId);
            }, WAIT_SECONDS_UNTIL_OPPONENT_ENTER_ROOM);
        }

        if (room.sessions.size() == ROOM_MAX_SIZE) {
            Objects.requireNonNull(room.timer).cancel(true);

            broadcast(roomId, "상대방 입장, 배틀 시작");
            battleService.startBattleRound(roomId);
        }
    }

    public void startBattle(Long roomId) {
        broadcast(roomId, "새로운 배틀 시작");
        battleService.startBattleRound(roomId);
    }

    public void playBattle(Long roomId, WebSocketSession session, String move) {
        Long userId = (Long) session.getAttributes().get("userId");

        if (!Move.isValid(move)) {
            try {
                session.sendMessage(new TextMessage("선택이 유효하지 않습니다. 선택을 다시 제출해주세요."));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        boolean bothMoved = battleService.move(
                roomId,
                userId,
                Move.valueOf(move)
        );

        Room room = rooms.get(roomId);

        if (!bothMoved) {
            room.timer = setTimer(roomId, () -> {
                broadcast(roomId, "상대가 선택하지 않아 배틀이 종료됩니다");
                battleService.decideWinner(roomId);
            }, WAIT_SECONDS_UNTIL_OPPONENT_MOVE);
        } else {
            Objects.requireNonNull(room.timer).cancel(true);
            BattleResultResponse response = battleService.decideWinner(roomId);
            broadcast(roomId, response);
        }
    }

    private ScheduledFuture<?> setTimer(Long roomId, Runnable task, Integer second) {
        return executor.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("{}분 타임아웃 처리 실패: roomId={}", second / 60, roomId, e);
            }
        }, second, TimeUnit.SECONDS);
    }

    private void broadcast(Long roomId, Object data) {
        String message = null;
        try {
            message = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (WebSocketSession s : rooms.get(roomId).sessions) {
            try {
                s.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("메시지 전송이 실패했습니다.", e);
            }
        }
    }

    private void closeSession(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException e) {
            log.error("세션을 닫는 데에 실패했습니다.", e);
        }
    }
}
