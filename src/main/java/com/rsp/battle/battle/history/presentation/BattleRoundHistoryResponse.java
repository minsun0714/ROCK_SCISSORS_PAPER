package com.rsp.battle.battle.history.presentation;

import com.rsp.battle.battle.domain.Move;

import java.time.Instant;

public record BattleRoundHistoryResponse(
        Long opponentId,
        String nickname,
        String profileImageUrl,
        Move myMove,
        Move opponentMove,
        BattleResult battleResult,
        Instant playedAt
) {
}