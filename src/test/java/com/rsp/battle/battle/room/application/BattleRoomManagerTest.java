package com.rsp.battle.battle.room.application;

import com.rsp.battle.battle.domain.BattleRound;
import com.rsp.battle.battle.domain.Move;
import com.rsp.battle.battle.request.presentation.dto.response.BattleResultResponse;
import com.rsp.battle.battle.room.application.BattleRoomManager;
import com.rsp.battle.battle.room.application.BattleService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BattleRoomManagerTest {

    @Mock
    private BattleService battleService;

    private BattleRoomManager manager;

    @BeforeEach
    void setUp() {
        manager = new BattleRoomManager(battleService);
    }

    private WebSocketSession mockSession(Long roomId, Long userId) {
        WebSocketSession session = mock(WebSocketSession.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("roomId", roomId);
        attributes.put("userId", userId);
        lenient().when(session.getAttributes()).thenReturn(attributes);
        lenient().when(session.isOpen()).thenReturn(true);
        return session;
    }

    // ── join ──

    @Test
    void joinFirstPlayerDoesNotStartBattle() {
        WebSocketSession session1 = mockSession(1L, 10L);

        manager.join(session1);

        verify(battleService, never()).startBattleRound(any());
    }

    @Test
    void joinSecondPlayerStartsBattle() {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session2 = mockSession(1L, 20L);

        manager.join(session1);
        manager.join(session2);

        verify(battleService).startBattleRound(1L);
    }

    @Test
    void joinSecondPlayerBroadcastsBattleStart() throws Exception {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session2 = mockSession(1L, 20L);

        manager.join(session1);
        manager.join(session2);

        verify(session1).sendMessage(any(TextMessage.class));
        verify(session2).sendMessage(any(TextMessage.class));
    }

    @Test
    void joinThirdPlayerIsRejected() throws Exception {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session2 = mockSession(1L, 20L);
        WebSocketSession session3 = mockSession(1L, 30L);

        manager.join(session1);
        manager.join(session2);
        manager.join(session3);

        verify(session3).close();
    }

    @Test
    void joinSameUserReplacesSession() throws Exception {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session1Reconnect = mockSession(1L, 10L);

        manager.join(session1);

        // 새로고침: 기존 세션이 닫힌 상태에서 재접속
        when(session1.isOpen()).thenReturn(false);
        manager.join(session1Reconnect);

        // 재접속이므로 여전히 1명 — 배틀 시작 안 됨
        verify(battleService, never()).startBattleRound(any());
    }

    @Test
    void joinDuplicateSessionIsRejected() throws Exception {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session1Duplicate = mockSession(1L, 10L);

        manager.join(session1);

        // 기존 세션이 열려있는 상태에서 다른 탭 접속 시도
        manager.join(session1Duplicate);

        // 중복 접속 거부 — 새 세션 닫힘
        verify(session1Duplicate).close();
        // 기존 세션은 유지
        verify(session1, never()).close();
    }

    // ── leave ──

    @Test
    void leaveRemovesSessionAndClosesRoom() throws Exception {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session2 = mockSession(1L, 20L);

        manager.join(session1);
        manager.join(session2);

        manager.leave(session1);

        // 상대에게 퇴장 알림 후 close
        verify(session2, atLeastOnce()).sendMessage(any(TextMessage.class));
        verify(session2).close();
    }

    @Test
    void leaveDoesNothingWhenRoomNotFound() {
        WebSocketSession session = mockSession(999L, 10L);

        // 예외 없이 정상 종료
        assertDoesNotThrow(() -> manager.leave(session));
    }

    // ── playBattle ──

    @Test
    void playBattleSubmitsMoveWhenValid() {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session2 = mockSession(1L, 20L);
        when(battleService.move(1L, 10L, Move.ROCK)).thenReturn(false);

        manager.join(session1);
        manager.join(session2);

        manager.playBattle(1L, session1, "ROCK");

        verify(battleService).move(1L, 10L, Move.ROCK);
    }

    @Test
    void playBattleSendsErrorWhenInvalidMove() throws Exception {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session2 = mockSession(1L, 20L);

        manager.join(session1);
        manager.join(session2);

        manager.playBattle(1L, session1, "LIZARD");

        verify(battleService, never()).move(any(), any(), any());
        verify(session1, atLeast(2)).sendMessage(any(TextMessage.class));
    }

    @Test
    void playBattleDecideWinnerWhenBothMoved() throws Exception {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session2 = mockSession(1L, 20L);

        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitRequesterMove(Move.ROCK);
        round.submitOpponentMove(Move.SCISSORS);
        round.decideWinner();

        when(battleService.move(1L, 20L, Move.SCISSORS)).thenReturn(true);
        when(battleService.decideWinner(1L)).thenReturn(BattleResultResponse.from(round));

        manager.join(session1);
        manager.join(session2);

        // 첫 번째 선택 시 타이머 생성을 위해
        when(battleService.move(1L, 10L, Move.ROCK)).thenReturn(false);
        manager.playBattle(1L, session1, "ROCK");

        // 두 번째 선택 시 결과 결정
        manager.playBattle(1L, session2, "SCISSORS");

        verify(battleService).decideWinner(1L);
    }

    // ── startBattle ──

    @Test
    void startBattleBroadcastsAndStartsRound() throws Exception {
        WebSocketSession session1 = mockSession(1L, 10L);
        WebSocketSession session2 = mockSession(1L, 20L);

        manager.join(session1);
        manager.join(session2);

        // reset to count only startBattle calls
        reset(battleService);

        manager.startBattle(1L);

        verify(battleService).startBattleRound(1L);
        verify(session1, atLeastOnce()).sendMessage(any(TextMessage.class));
        verify(session2, atLeastOnce()).sendMessage(any(TextMessage.class));
    }

    @Test
    void joinRaceConditionShouldStartBattleOnlyOnce() throws Exception {

        int repeat = 10000;

        for (int i = 0; i < repeat; i++) {

            Long roomId = (long) i;

            WebSocketSession session1 = mockSession(roomId, 10L);
            WebSocketSession session2 = mockSession(roomId, 20L);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            CyclicBarrier barrier = new CyclicBarrier(2);

            Future<?> f1 = executor.submit(() -> {
                barrier.await();
                manager.join(session1);
                return null;
            });

            Future<?> f2 = executor.submit(() -> {
                barrier.await();
                manager.join(session2);
                return null;
            });

            f1.get();
            f2.get();

            executor.shutdown();

            verify(battleService, atMostOnce()).startBattleRound(roomId);

            reset(battleService);
        }
    }

    @Test
    void playBattleRaceConditionShouldStartBattleOnlyOnce() throws Exception {

        int repeat = 10000;
        int fail = 0;

        for (int i = 0; i < repeat; i++) {

            Long roomId = (long) i;

            WebSocketSession session1 = mockSession(roomId, 10L);
            WebSocketSession session2 = mockSession(roomId, 20L);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            CyclicBarrier barrier = new CyclicBarrier(2);

            Future<?> f1 = executor.submit(() -> {
                barrier.await();
                manager.join(session1);
                return null;
            });

            Future<?> f2 = executor.submit(() -> {
                barrier.await();
                manager.join(session2);
                return null;
            });

            f1.get();
            f2.get();

            executor.shutdown();

            try {
                verify(battleService, times(1)).startBattleRound(roomId);
            } catch (Throwable e){
                fail++;
            }

            reset(battleService);
        }
        Assertions.assertEquals(0, fail);
    }
}
