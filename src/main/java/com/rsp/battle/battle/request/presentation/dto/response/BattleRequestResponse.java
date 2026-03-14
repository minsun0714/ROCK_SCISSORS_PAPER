package com.rsp.battle.battle.request.presentation.dto.response;

import com.rsp.battle.battle.domain.BattleRoom;
import com.rsp.battle.battle.request.presentation.BattleStatus;

import java.time.Instant;

public record BattleRequestResponse(
        Long roomId,
        Long opponentId,
        BattleStatus battleStatus,
        Instant createdAt
) {
    public static BattleRequestResponse from(BattleRoom battleRoom) {
        return new BattleRequestResponse(
                battleRoom.getId(),
                battleRoom.getOpponent(),
                BattleStatus.REQUESTED,
                battleRoom.getCreatedAt()
        );
    }
}