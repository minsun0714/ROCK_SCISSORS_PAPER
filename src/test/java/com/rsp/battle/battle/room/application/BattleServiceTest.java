package com.rsp.battle.battle.room.application;

import com.rsp.battle.battle.domain.BattleRoom;
import com.rsp.battle.battle.domain.BattleRound;
import com.rsp.battle.battle.domain.Move;
import com.rsp.battle.battle.persistence.BattleRoomRepository;
import com.rsp.battle.battle.persistence.BattleRoundRepository;
import com.rsp.battle.battle.request.presentation.dto.response.BattleResultResponse;
import com.rsp.battle.battle.room.application.BattleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BattleServiceTest {

    @Mock
    private BattleRoomRepository battleRoomRepository;

    @Mock
    private BattleRoundRepository battleRoundRepository;

    @InjectMocks
    private BattleService battleService;

    // ── startBattleRound ──

    @Test
    void startBattleRoundCreatesRoundWithIncreasedNumber() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findByIdForUpdate(100L)).thenReturn(room);

        battleService.startBattleRound(100L);

        ArgumentCaptor<BattleRound> captor = ArgumentCaptor.forClass(BattleRound.class);
        verify(battleRoundRepository).save(captor.capture());

        BattleRound saved = captor.getValue();
        assertEquals(100L, saved.getRoomId());
        assertEquals(1L, saved.getRoundNumber());
        assertEquals(1L, saved.getRequesterId());
        assertEquals(2L, saved.getOpponentId());
    }

    @Test
    void startBattleRoundIncrementsRoundNumberOnSecondCall() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findByIdForUpdate(100L)).thenReturn(room);

        battleService.startBattleRound(100L);
        battleService.startBattleRound(100L);

        ArgumentCaptor<BattleRound> captor = ArgumentCaptor.forClass(BattleRound.class);
        verify(battleRoundRepository, times(2)).save(captor.capture());

        assertEquals(1L, captor.getAllValues().get(0).getRoundNumber());
        assertEquals(2L, captor.getAllValues().get(1).getRoundNumber());
    }

    // ── move ──

    @Test
    void moveSubmitsRequesterMoveAndReturnsFalse() {
        BattleRound round = BattleRound.create(100L, 1L, 1L, 2L);
        when(battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(100L)).thenReturn(round);

        boolean result = battleService.move(100L, 1L, Move.ROCK);

        assertFalse(result);
        assertEquals(Move.ROCK, round.getRequesterMove());
    }

    @Test
    void moveSubmitsOpponentMoveAndReturnsFalse() {
        BattleRound round = BattleRound.create(100L, 1L, 1L, 2L);
        when(battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(100L)).thenReturn(round);

        boolean result = battleService.move(100L, 2L, Move.SCISSORS);

        assertFalse(result);
        assertEquals(Move.SCISSORS, round.getOpponentMove());
    }

    @Test
    void moveReturnsTrueWhenBothMoved() {
        BattleRound round = BattleRound.create(100L, 1L, 1L, 2L);
        round.submitRequesterMove(Move.ROCK);
        when(battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(100L)).thenReturn(round);

        boolean result = battleService.move(100L, 2L, Move.PAPER);

        assertTrue(result);
    }

    // ── decideWinner ──

    @Test
    void decideWinnerReturnsResponseWithRequesterWin() {
        BattleRound round = BattleRound.create(100L, 1L, 1L, 2L);
        round.submitRequesterMove(Move.ROCK);
        round.submitOpponentMove(Move.SCISSORS);
        when(battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(100L)).thenReturn(round);

        BattleResultResponse response = battleService.decideWinner(100L);

        assertEquals(1L, response.winnerUserId());
        assertEquals(Move.ROCK, response.requesterMove());
        assertEquals(Move.SCISSORS, response.opponentMove());
    }

    @Test
    void decideWinnerReturnsResponseWithOpponentWin() {
        BattleRound round = BattleRound.create(100L, 1L, 1L, 2L);
        round.submitRequesterMove(Move.ROCK);
        round.submitOpponentMove(Move.PAPER);
        when(battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(100L)).thenReturn(round);

        BattleResultResponse response = battleService.decideWinner(100L);

        assertEquals(2L, response.winnerUserId());
    }

    @Test
    void decideWinnerReturnsResponseWithDraw() {
        BattleRound round = BattleRound.create(100L, 1L, 1L, 2L);
        round.submitRequesterMove(Move.ROCK);
        round.submitOpponentMove(Move.ROCK);
        when(battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(100L)).thenReturn(round);

        BattleResultResponse response = battleService.decideWinner(100L);

        assertNull(response.winnerUserId());
    }

    @Test
    void decideWinnerHandlesRequesterTimeout() {
        BattleRound round = BattleRound.create(100L, 1L, 1L, 2L);
        round.submitOpponentMove(Move.ROCK);
        when(battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(100L)).thenReturn(round);

        BattleResultResponse response = battleService.decideWinner(100L);

        assertEquals(2L, response.winnerUserId());
    }

    @Test
    void decideWinnerHandlesOpponentTimeout() {
        BattleRound round = BattleRound.create(100L, 1L, 1L, 2L);
        round.submitRequesterMove(Move.ROCK);
        when(battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(100L)).thenReturn(round);

        BattleResultResponse response = battleService.decideWinner(100L);

        assertEquals(1L, response.winnerUserId());
    }
}
