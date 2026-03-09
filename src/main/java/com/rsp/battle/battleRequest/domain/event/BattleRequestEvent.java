package com.rsp.battle.battleRequest.domain.event;

import com.rsp.battle.notification.presentation.NotificationType;

public record BattleRequestEvent(
        Long receiverId,
        NotificationType type,
        Long senderId
) {
}
