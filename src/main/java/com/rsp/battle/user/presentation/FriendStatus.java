package com.rsp.battle.user.presentation;

import com.rsp.battle.friendRequest.domain.FriendRequestStatus;

/**
 * DB 테이블에서 사용하는 상태와 분리.
 * FreiendRequest에는 3가지 상태 (PENDING, ACCEPTED, REJECTED)가 있음
 */
public enum FriendStatus {
    NONE,
    REQUESTED,
    PENDING,
    FRIEND;

    public static FriendStatus from(FriendRequestStatus status, Long loginUserId, Long requesterId) {
        return switch (status) {
            case ACCEPTED -> FRIEND;
            case PENDING -> loginUserId.equals(requesterId) ? REQUESTED : PENDING;
            case REJECTED -> NONE;
        };
    }
}
