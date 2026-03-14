package com.rsp.battle.battle.presentation.dto.response;

import com.rsp.battle.battle.domain.BattleRoom;
import com.rsp.battle.battle.presentation.BattleStatus;

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
                Instant.now()
        );
    }
}
