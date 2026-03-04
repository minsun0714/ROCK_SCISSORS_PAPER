package com.rsp.battle.friendRequest.presentation;

import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.user.presentation.FriendStatus;

import java.time.Instant;

public record FriendRequestRejectResponse(
        FriendStatus friendStatus,
        Instant closedAt
) {
    public static FriendRequestRejectResponse from(FriendRequest friendRequest) {
        return new FriendRequestRejectResponse(
                FriendStatus.NONE,
                friendRequest.getClosedAt()
        );
    }
}
