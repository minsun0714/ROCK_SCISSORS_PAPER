package com.rsp.battle.friendRequest.domain.event;

import com.rsp.battle.notification.presentation.NotificationType;

public record FriendRequestEvent(
        Long receiverId,
        NotificationType type,
        Long senderId
) {
}
