package com.rsp.battle.notification.presentation.dto;

import com.rsp.battle.notification.presentation.NotificationType;

public record SseSend(
        NotificationType eventName,
        Object data
) {
}
