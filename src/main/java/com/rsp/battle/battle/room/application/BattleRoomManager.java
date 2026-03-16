package com.rsp.battle.battle.room.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rsp.battle.battle.room.presentation.WebSocketMessageType;
import com.rsp.battle.battle.room.presentation.WebSocketResponse;
import com.rsp.battle.battle.domain.Move;
import com.rsp.battle.battle.request.presentation.dto.response.BattleResultResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
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
    private final static Integer WAIT_SECONDS_UNTIL_OPPONENT_ENTER_ROOM = 60 * 5;
    private final static Integer WAIT_SECONDS_UNTIL_OPPONENT_MOVE = 30;

    public void join(WebSocketSession currentSession) {
        Long roomId = (Long) currentSession.getAttributes().get("roomId");
        Long userId = (Long) currentSession.getAttributes().get("userId");

        Room room = rooms.computeIfAbsent(roomId, r -> new Room()); // getOrDefault에서 동시성 이슈로 변경

        Optional<WebSocketSession> myOldSession = room.sessions.stream()
                .filter(s -> s.getAttributes().get("userId").equals(userId))
                .findFirst();

        if (myOldSession.isPresent()) {
            if (myOldSession.get().isOpen()) {
                closeSession(currentSession);
                return;
            }
            room.sessions.remove(myOldSession.get());
        }

        if (room.sessions.size() >= ROOM_MAX_SIZE
                || !room.sessions.isEmpty() && room.timer != null && room.timer.isDone()) {
            closeSession(currentSession);
            return;
        }

        room.sessions.add(currentSession);

        if (room.sessions.size() < ROOM_MAX_SIZE) {
            room.timer = setTimer(roomId, () -> {
                broadcast(roomId, WebSocketResponse.of(
                        WebSocketMessageType.ROOM_CLOSED,
                        "상대방이 5분 동안 입장하지 않아 배틀이 종료됩니다."
                ));
                for (WebSocketSession s : room.sessions) {
                    closeSession(s);
                }
                rooms.remove(roomId);
            }, WAIT_SECONDS_UNTIL_OPPONENT_ENTER_ROOM);
        }

        if (room.sessions.size() == ROOM_MAX_SIZE) {
            closeTimer(roomId);

            startBattle(roomId);
        }
    }

    public void startBattle(Long roomId) {
        battleService.startBattleRound(roomId);
        broadcast(roomId, WebSocketResponse.of(
                WebSocketMessageType.BATTLE_START,
                "새로운 배틀 시작"
        ));

        rooms.get(roomId).timer = setTimer(roomId, () -> {
            BattleResultResponse response = battleService.decideWinner(roomId);
            broadcast(roomId, WebSocketResponse.of(
                    WebSocketMessageType.BATTLE_FINISHED,
                    response
            ));
        }, WAIT_SECONDS_UNTIL_OPPONENT_MOVE);
    }

    public void playBattle(Long roomId, WebSocketSession session, String move) {
        if (rooms.get(roomId).sessions.size() < ROOM_MAX_SIZE) {
            return;
        }

        Long userId = (Long) session.getAttributes().get("userId");

        if (!Move.isValid(move)) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                        WebSocketResponse.of(
                                WebSocketMessageType.ERROR,
                                "선택이 유효하지 않습니다."
                        )
                )));
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

        closeTimer(roomId);

        if (!bothMoved) {
            room.timer = setTimer(roomId, () -> {
                BattleResultResponse response = battleService.decideWinner(roomId);
                broadcast(roomId, WebSocketResponse.of(
                        WebSocketMessageType.BATTLE_FINISHED,
                        response
                ));
            }, WAIT_SECONDS_UNTIL_OPPONENT_MOVE);
        } else {
            BattleResultResponse response = battleService.decideWinner(roomId);
            broadcast(roomId, WebSocketResponse.of(
                    WebSocketMessageType.BATTLE_FINISHED,
                    response
            ));
        }
    }

    public void leave(WebSocketSession session) {
        Long roomId = (Long) session.getAttributes().get("roomId");
        Room room = rooms.get(roomId);

        if (room == null) return;

        if (room.timer != null) room.timer.cancel(true);

        boolean isRemoved = room.sessions.remove(session);
        if (!isRemoved) return;

        if (room.sessions.isEmpty()) {
            battleService.close(roomId);
            rooms.remove(roomId);
            return;
        }

        BattleResultResponse forfeitResult = battleService.forfeit(roomId);
        if (forfeitResult != null) {
            broadcast(roomId, WebSocketResponse.of(
                    WebSocketMessageType.BATTLE_FINISHED,
                    forfeitResult
            ));
        }
        broadcast(roomId, WebSocketResponse.of(
                WebSocketMessageType.ROOM_CLOSED,
                "상대방이 퇴장했습니다."
        ));
        for (WebSocketSession s : room.sessions) {
            closeSession(s);
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

    private void closeTimer(Long roomId) {
        ScheduledFuture<?> timer = rooms.get(roomId).timer;
        if (timer != null) timer.cancel(true);
    }

    private void broadcast(Long roomId, Object data) {
        Room room = rooms.get(roomId);
        if (room == null) return;

        String message = null;
        try {
            message = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        for (WebSocketSession s : room.sessions) {
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
