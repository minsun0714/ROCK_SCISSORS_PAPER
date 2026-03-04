package com.rsp.battle.friendRequest.presentation;

import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.user.presentation.FriendStatus;

import java.time.Instant;

public record FriendRequestAcceptResponse(
        FriendStatus friendStatus,
        Instant closedAt
) {
    public static FriendRequestAcceptResponse from(FriendRequest friendRequest) {
        return new FriendRequestAcceptResponse(
                FriendStatus.FRIEND,
                friendRequest.getClosedAt()
        );
    }
}
