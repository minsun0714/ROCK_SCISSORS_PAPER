package com.rsp.battle.battleRequest.presentation.dto.response;

import com.rsp.battle.battleRequest.domain.BattleRoom;
import com.rsp.battle.battleRequest.presentation.BattleStatus;

import java.time.Instant;

public record BattleRequestAcceptResponse(
        Long roomId,
        BattleStatus battleStatus,
        Instant startedAt
) {
    public static BattleRequestAcceptResponse from(BattleRoom battleRoom) {
        return new BattleRequestAcceptResponse(
                battleRoom.getId(),
                BattleStatus.IN_PROGRESS,
                Instant.now()
        );
    }
}
