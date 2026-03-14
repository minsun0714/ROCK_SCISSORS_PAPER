package com.rsp.battle.battle.history.application;

import com.rsp.battle.battle.domain.Move;
import com.rsp.battle.battle.history.presentation.BattleResult;
import com.rsp.battle.battle.history.presentation.BattleRoundHistoryResponse;
import com.rsp.battle.battle.history.presentation.BattleRoundStatResponse;
import com.rsp.battle.battle.persistence.BattleRoundHistoryProjection;
import com.rsp.battle.battle.persistence.BattleRoundRepository;
import com.rsp.battle.battle.persistence.BattleStatProjection;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BattleRoundQueryServiceTest {

    @Mock
    private BattleRoundRepository battleRoundRepository;

    @InjectMocks
    private BattleRoundQueryService battleRoundQueryService;

    // ── getBattleStat ──

    @Test
    void getBattleStatReturnsCorrectWinRate() {
        BattleStatProjection projection = mock(BattleStatProjection.class);
        when(projection.getTotalCount()).thenReturn(10L);
        when(projection.getWinCount()).thenReturn(7L);
        when(projection.getLoseCount()).thenReturn(2L);
        when(projection.getDrawCount()).thenReturn(1L);
        when(battleRoundRepository.findBattleStatByUserId(1L)).thenReturn(projection);

        BattleRoundStatResponse response = battleRoundQueryService.getBattleStat(1L);

        assertEquals(1L, response.userId());
        assertEquals(10L, response.totalCount());
        assertEquals(7L, response.winCount());
        assertEquals(2L, response.loseCount());
        assertEquals(1L, response.drawCount());
        assertEquals(0.7, response.winRate(), 0.001);
    }

    @Test
    void getBattleStatReturnsZeroWinRateWhenNoGames() {
        BattleStatProjection projection = mock(BattleStatProjection.class);
        when(projection.getTotalCount()).thenReturn(0L);
        when(projection.getWinCount()).thenReturn(0L);
        when(projection.getLoseCount()).thenReturn(0L);
        when(projection.getDrawCount()).thenReturn(0L);
        when(battleRoundRepository.findBattleStatByUserId(1L)).thenReturn(projection);

        BattleRoundStatResponse response = battleRoundQueryService.getBattleStat(1L);

        assertEquals(0L, response.totalCount());
        assertEquals(0.0, response.winRate());
    }

    // ── getBattleHistory ──

    @Test
    void getBattleHistorySwapsMovesWhenUserIsRequester() {
        BattleRoundHistoryProjection projection = mockProjection(
                100L, 1L, 2L, Move.ROCK, Move.SCISSORS, 1L
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<BattleRoundHistoryProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
        when(battleRoundRepository.searchBattleResult(1L, "", null, pageable)).thenReturn(page);

        Paginated<BattleRoundHistoryResponse> result = battleRoundQueryService.getBattleHistory(1L, "", null, pageable);

        BattleRoundHistoryResponse history = result.content().get(0);
        assertEquals(Move.ROCK, history.myMove());
        assertEquals(Move.SCISSORS, history.opponentMove());
    }

    @Test
    void getBattleHistorySwapsMovesWhenUserIsOpponent() {
        BattleRoundHistoryProjection projection = mockProjection(
                100L, 10L, 1L, Move.ROCK, Move.PAPER, 1L
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<BattleRoundHistoryProjection> page = new PageImpl<>(List.of(projection), pageable, 1);
        when(battleRoundRepository.searchBattleResult(1L, "", null, pageable)).thenReturn(page);

        Paginated<BattleRoundHistoryResponse> result = battleRoundQueryService.getBattleHistory(1L, "", null, pageable);

        BattleRoundHistoryResponse history = result.content().get(0);
        assertEquals(Move.PAPER, history.myMove());
        assertEquals(Move.ROCK, history.opponentMove());
    }

    @Test
    void getBattleHistoryPassesBattleResultNameToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BattleRoundHistoryProjection> page = new PageImpl<>(List.of(), pageable, 0);
        when(battleRoundRepository.searchBattleResult(1L, "", "WIN", pageable)).thenReturn(page);

        battleRoundQueryService.getBattleHistory(1L, "", BattleResult.WIN, pageable);

        verify(battleRoundRepository).searchBattleResult(1L, "", "WIN", pageable);
    }

    @Test
    void getBattleHistoryPassesNullWhenBattleResultIsNull() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BattleRoundHistoryProjection> page = new PageImpl<>(List.of(), pageable, 0);
        when(battleRoundRepository.searchBattleResult(1L, "", null, pageable)).thenReturn(page);

        battleRoundQueryService.getBattleHistory(1L, "", null, pageable);

        verify(battleRoundRepository).searchBattleResult(1L, "", null, pageable);
    }

    @Test
    void getBattleHistoryReturnsPaginatedResponse() {
        BattleRoundHistoryProjection p1 = mockProjection(100L, 1L, 2L, Move.ROCK, Move.SCISSORS, 1L);
        BattleRoundHistoryProjection p2 = mockProjection(101L, 1L, 3L, Move.PAPER, Move.ROCK, 1L);
        Pageable pageable = PageRequest.of(0, 10);
        Page<BattleRoundHistoryProjection> page = new PageImpl<>(List.of(p1, p2), pageable, 2);
        when(battleRoundRepository.searchBattleResult(1L, "", null, pageable)).thenReturn(page);

        Paginated<BattleRoundHistoryResponse> result = battleRoundQueryService.getBattleHistory(1L, "", null, pageable);

        assertEquals(2, result.content().size());
        assertEquals(2L, result.totalElements());
        assertFalse(result.hasNext());
    }

    private BattleRoundHistoryProjection mockProjection(
            Long id, Long requesterId, Long opponentId,
            Move requesterMove, Move opponentMove, Long winnerUserId
    ) {
        BattleRoundHistoryProjection projection = mock(BattleRoundHistoryProjection.class);
        lenient().when(projection.getId()).thenReturn(id);
        lenient().when(projection.getRequesterId()).thenReturn(requesterId);
        lenient().when(projection.getOpponentId()).thenReturn(opponentId);
        lenient().when(projection.getNickName()).thenReturn("opponent");
        lenient().when(projection.getProfileImageKey()).thenReturn("img.png");
        lenient().when(projection.getRequesterMove()).thenReturn(requesterMove);
        lenient().when(projection.getOpponentMove()).thenReturn(opponentMove);
        lenient().when(projection.getWinnerUserId()).thenReturn(winnerUserId);
        lenient().when(projection.getCreatedAt()).thenReturn(Instant.now());
        return projection;
    }
}
