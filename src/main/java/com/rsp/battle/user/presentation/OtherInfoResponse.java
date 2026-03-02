package com.rsp.battle.user.presentation;

import com.rsp.battle.user.domain.User;

public record OtherInfoResponse(
        Long userId,
        String nickname,
        String profileImageKey,
        String statusMessage,
        PresenceStatus presenceStatus,
        FriendStatus friendStatus
) {
    public static OtherInfoResponse from(User user, String profileImageUrl, PresenceStatus presenceStatus, FriendStatus friendStatus) {
        return new OtherInfoResponse(
                user.getId(),
                user.getNickname(),
                profileImageUrl,
                user.getStatusMessage(),
                presenceStatus,
                friendStatus
        );
    }
}
