package com.rsp.battle.battle.presentation.dto.response;

import com.rsp.battle.battle.domain.BattleRound;
import com.rsp.battle.battle.domain.Move;

public record BattleResultResponse(
        Long roomId,
        Long roundNumber,
        Long requesterId,
        Long opponentId,
        Move requesterMove,
        Move opponentMove,
        Long winnerUserId
) {
    public static BattleResultResponse from(BattleRound battleRound) {
        return new BattleResultResponse(
                battleRound.getRoomId(),
                battleRound.getRoundNumber(),
                battleRound.getRequesterId(),
                battleRound.getOpponentId(),
                battleRound.getRequesterMove(),
                battleRound.getOpponentMove(),
                battleRound.getWinnerUserId()
        );
    }
}
