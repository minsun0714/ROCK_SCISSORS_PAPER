package com.rsp.battle.battle.domain.event;

public record BattleRequestNotificationData(
        Long requestId,
        Long senderId,
        String nickname,
        String profileImageUrl
) {
}
