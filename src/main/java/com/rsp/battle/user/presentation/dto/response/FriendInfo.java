package com.rsp.battle.user.presentation.dto.response;

import com.rsp.battle.user.presentation.FriendStatus;

public record FriendInfo(
        FriendStatus status,
        Long friendRequestId
) {
    public static FriendInfo none() {
        return new FriendInfo(FriendStatus.NONE, null);
    }

    public static FriendInfo of(FriendStatus status, Long friendRequestId) {
        return new FriendInfo(status, friendRequestId);
    }
}
