package com.rsp.battle.friendRequest.domain.event;

public record FriendRequestNotificationData(
        Long senderId,
        String nickname,
        String profileImageUrl
) {
}
