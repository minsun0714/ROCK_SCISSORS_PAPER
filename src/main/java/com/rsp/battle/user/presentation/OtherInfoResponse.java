package com.rsp.battle.user.presentation;

import com.rsp.battle.user.domain.PresenceStatus;
import com.rsp.battle.user.domain.User;

public record OtherInfoResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String statusMessage,
        PresenceStatus presenceStatus,
        FriendInfo friendInfo
) {
    public static OtherInfoResponse from(User user, String profileImageUrl, PresenceStatus presenceStatus, FriendInfo friendInfo) {
        return new OtherInfoResponse(
                user.getId(),
                user.getNickname(),
                profileImageUrl,
                user.getStatusMessage(),
                presenceStatus,
                friendInfo
        );
    }
}
