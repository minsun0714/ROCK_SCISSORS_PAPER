package com.rsp.battle.friendRequest.presentation.dto.response;

import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.friendRequest.domain.FriendRequestStatus;

import java.time.Instant;

public record FriendRequestResponse(
        Long requestId,
        FriendRequestStatus friendRequestStatus,
        Instant createdAt
) {
    public static FriendRequestResponse from(FriendRequest friendRequest) {
        return new FriendRequestResponse(
                friendRequest.getId(),
                friendRequest.getStatus(),
                friendRequest.getCreatedAt()
        );
    }
}
