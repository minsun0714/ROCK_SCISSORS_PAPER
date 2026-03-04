package com.rsp.battle.friendRequest.domain;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FriendRequestTest {

    @Test
    void createBuildsPendingRequestWithOrderedPairIds() {
        FriendRequest friendRequest = FriendRequest.create(10L, 2L);

        assertEquals(10L, friendRequest.getRequester());
        assertEquals(2L, friendRequest.getReceiver());
        assertEquals(2L, friendRequest.getUserLowId());
        assertEquals(10L, friendRequest.getUserHighId());
        assertEquals(FriendRequestStatus.PENDING, friendRequest.getStatus());
    }

    @Test
    void createThrowsWhenRequesterAndReceiverAreSame() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> FriendRequest.create(3L, 3L)
        );

        assertEquals(ErrorCode.SELF_FRIEND_REQUEST_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    void acceptUpdatesStatusAndClosedAt() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);

        friendRequest.accept();

        assertEquals(FriendRequestStatus.ACCEPTED, friendRequest.getStatus());
        assertNotNull(friendRequest.getClosedAt());
    }

    @Test
    void acceptThrowsWhenRequestAlreadyClosed() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        friendRequest.reject();

        BusinessException exception = assertThrows(
                BusinessException.class,
                friendRequest::accept
        );

        assertEquals(ErrorCode.FRIEND_REQUEST_CLOSED, exception.getErrorCode());
    }

    @Test
    void rejectUpdatesStatusAndClosedAt() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);

        friendRequest.reject();

        assertEquals(FriendRequestStatus.REJECTED, friendRequest.getStatus());
        assertNotNull(friendRequest.getClosedAt());
    }

    @Test
    void rejectThrowsWhenRequestAlreadyClosed() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        friendRequest.accept();

        BusinessException exception = assertThrows(
                BusinessException.class,
                friendRequest::reject
        );

        assertEquals(ErrorCode.FRIEND_REQUEST_CLOSED, exception.getErrorCode());
    }
}
