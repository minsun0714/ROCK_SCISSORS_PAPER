package com.rsp.battle.battle.domain.event;

import com.rsp.battle.notification.presentation.NotificationType;

public record BattleRequestEvent(
        Long receiverId,
        NotificationType type,
        Long senderId,
        Long roomId
) {
}
