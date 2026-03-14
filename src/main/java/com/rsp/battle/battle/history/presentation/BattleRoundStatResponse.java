package com.rsp.battle.battle.history.presentation;

public record BattleRoundStatResponse(
        Long userId,
        Long totalCount,
        Long winCount,
        Long loseCount,
        Long drawCount,
        double winRate
) {
    public static BattleRoundStatResponse of(
            Long userId,
            Long totalCount,
            Long winCount,
            Long loseCount,
            Long drawCount,
            double winRate
    ) {
        return new BattleRoundStatResponse(
                userId,
                totalCount,
                winCount,
                loseCount,
                drawCount,
                winRate
        );
    }
}