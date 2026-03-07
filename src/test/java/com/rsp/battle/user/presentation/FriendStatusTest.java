package com.rsp.battle.user.presentation;

import com.rsp.battle.friendRequest.domain.FriendRequestStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FriendStatusTest {

    @Test
    void from_accepted_returnsFriend() {
        assertEquals(FriendStatus.FRIEND, FriendStatus.from(FriendRequestStatus.ACCEPTED, 1L, 1L));
        assertEquals(FriendStatus.FRIEND, FriendStatus.from(FriendRequestStatus.ACCEPTED, 1L, 2L));
    }

    @Test
    void from_pending_returnsRequestedWhenIRequested() {
        assertEquals(FriendStatus.REQUESTED, FriendStatus.from(FriendRequestStatus.PENDING, 1L, 1L));
    }

    @Test
    void from_pending_returnsPendingWhenOtherRequested() {
        assertEquals(FriendStatus.PENDING, FriendStatus.from(FriendRequestStatus.PENDING, 1L, 2L));
    }

    @Test
    void from_rejected_returnsNone() {
        assertEquals(FriendStatus.NONE, FriendStatus.from(FriendRequestStatus.REJECTED, 1L, 1L));
        assertEquals(FriendStatus.NONE, FriendStatus.from(FriendRequestStatus.REJECTED, 1L, 2L));
    }
}
