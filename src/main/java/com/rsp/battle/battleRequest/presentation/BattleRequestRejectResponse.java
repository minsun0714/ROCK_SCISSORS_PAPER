package com.rsp.battle.battleRequest.presentation;

import com.rsp.battle.battleRequest.domain.BattleRoom;

import java.time.Instant;

public record BattleRequestRejectResponse(
        Long roomId,
        BattleStatus battleStatus,
        Instant closedAt
) {
    public static BattleRequestRejectResponse from(BattleRoom battleRoom) {
        return new BattleRequestRejectResponse(
                battleRoom.getId(),
                BattleStatus.REJECTED,
                battleRoom.getClosedAt()
        );
    }
}
