package com.rsp.battle.battleRequest.domain.event;

public record BattleRequestNotificationData(
        Long requestId,
        Long senderId,
        String nickname,
        String profileImageUrl
) {
}
