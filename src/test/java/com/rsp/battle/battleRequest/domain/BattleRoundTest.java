package com.rsp.battle.battleRequest.domain;

import com.rsp.battle.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BattleRoundTest {

    // ── 생성 ──

    @Test
    void createBuildsBattleRoundWithGivenFields() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);

        assertEquals(1L, round.getRoomId());
        assertEquals(1L, round.getRoundNumber());
        assertEquals(10L, round.getRequesterId());
        assertEquals(20L, round.getOpponentId());
        assertNull(round.getRequesterMove());
        assertNull(round.getOpponentMove());
        assertNull(round.getWinnerUserId());
    }

    // ── move 제출 ──

    @Test
    void submitRequesterMoveSetsMove() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);

        round.submitRequesterMove(Move.ROCK);

        assertEquals(Move.ROCK, round.getRequesterMove());
    }

    @Test
    void submitOpponentMoveSetsMove() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);

        round.submitOpponentMove(Move.SCISSORS);

        assertEquals(Move.SCISSORS, round.getOpponentMove());
    }

    // ── isComplete ──

    @Test
    void isCompleteReturnsFalseWhenNoMoves() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);

        assertFalse(round.isComplete());
    }

    @Test
    void isCompleteReturnsFalseWhenOnlyRequesterMoved() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitRequesterMove(Move.ROCK);

        assertFalse(round.isComplete());
    }

    @Test
    void isCompleteReturnsFalseWhenOnlyOpponentMoved() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitOpponentMove(Move.PAPER);

        assertFalse(round.isComplete());
    }

    @Test
    void isCompleteReturnsTrueWhenBothMoved() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitRequesterMove(Move.ROCK);
        round.submitOpponentMove(Move.PAPER);

        assertTrue(round.isComplete());
    }

    // ── decideWinner: 정상 대결 ──

    @Test
    void decideWinnerSetsRequesterWhenRequesterWins() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitRequesterMove(Move.ROCK);
        round.submitOpponentMove(Move.SCISSORS);

        round.decideWinner();

        assertEquals(10L, round.getWinnerUserId());
    }

    @Test
    void decideWinnerSetsOpponentWhenOpponentWins() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitRequesterMove(Move.ROCK);
        round.submitOpponentMove(Move.PAPER);

        round.decideWinner();

        assertEquals(20L, round.getWinnerUserId());
    }

    @Test
    void decideWinnerSetsNullWhenDraw() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitRequesterMove(Move.ROCK);
        round.submitOpponentMove(Move.ROCK);

        round.decideWinner();

        assertNull(round.getWinnerUserId());
    }

    // ── decideWinner: 타임아웃 (한쪽만 제출) ──

    @Test
    void decideWinnerSetsOpponentWhenRequesterTimedOut() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitOpponentMove(Move.ROCK);

        round.decideWinner();

        assertEquals(20L, round.getWinnerUserId());
    }

    @Test
    void decideWinnerSetsRequesterWhenOpponentTimedOut() {
        BattleRound round = BattleRound.create(1L, 1L, 10L, 20L);
        round.submitRequesterMove(Move.ROCK);

        round.decideWinner();

        assertEquals(10L, round.getWinnerUserId());
    }

    // ── Move.fight 전체 조합 ──

    @Test
    void rockBeatsScissors() {
        assertEquals(1, Move.ROCK.fight(Move.SCISSORS));
    }

    @Test
    void scissorsBeatsPaper() {
        assertEquals(1, Move.SCISSORS.fight(Move.PAPER));
    }

    @Test
    void paperBeatsRock() {
        assertEquals(1, Move.PAPER.fight(Move.ROCK));
    }

    @Test
    void sameMovesDrawRock() {
        assertEquals(0, Move.ROCK.fight(Move.ROCK));
    }

    @Test
    void sameMovesDrawPaper() {
        assertEquals(0, Move.PAPER.fight(Move.PAPER));
    }

    @Test
    void sameMovesDrawScissors() {
        assertEquals(0, Move.SCISSORS.fight(Move.SCISSORS));
    }

    // ── Move.isValid ──

    @Test
    void isValidReturnsTrueForValidMove() {
        assertTrue(Move.isValid("ROCK"));
        assertTrue(Move.isValid("PAPER"));
        assertTrue(Move.isValid("SCISSORS"));
    }

    @Test
    void isValidReturnsFalseForInvalidMove() {
        assertFalse(Move.isValid("LIZARD"));
        assertFalse(Move.isValid(""));
        assertFalse(Move.isValid("rock"));
    }

    // ── BattleRoom.increaseRoundNumber ──

    @Test
    void increaseRoundNumberIncrementsSequentially() {
        BattleRoom room = BattleRoom.create(1L, 2L);

        assertEquals(1L, room.increaseRoundNumber());
        assertEquals(2L, room.increaseRoundNumber());
        assertEquals(3L, room.increaseRoundNumber());
    }
}
