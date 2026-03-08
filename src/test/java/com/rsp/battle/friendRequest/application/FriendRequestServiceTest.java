package com.rsp.battle.friendRequest.application;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.friendRequest.domain.FriendRequestStatus;
import com.rsp.battle.friendRequest.persistence.FriendRequestRepository;
import com.rsp.battle.friendRequest.presentation.FriendRequestAcceptResponse;
import com.rsp.battle.friendRequest.presentation.FriendRequestRejectResponse;
import com.rsp.battle.friendRequest.presentation.dto.response.FriendRequestResponse;
import com.rsp.battle.friendRequest.domain.event.FriendRequestEvent;
import com.rsp.battle.notification.presentation.NotificationType;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.FriendStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendRequestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private FriendRequestService friendRequestService;

    @Test
    void createFriendRequestReturnsPendingResponseWhenSuccess() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FriendRequestResponse response = friendRequestService.createFriendRequest(1L, 2L);

        assertEquals(FriendRequestStatus.PENDING, response.friendRequestStatus());
        verify(friendRequestRepository).save(any(FriendRequest.class));
    }

    @Test
    void createFriendRequestThrowsWhenTargetUserDoesNotExist() {
        when(userRepository.existsById(2L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> friendRequestService.createFriendRequest(1L, 2L)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(friendRequestRepository, never()).save(any(FriendRequest.class));
    }

    @Test
    void createFriendRequestThrowsWhenDuplicateRequestExists() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> friendRequestService.createFriendRequest(1L, 2L)
        );

        assertEquals(ErrorCode.DUPLICATE_FRIEND_REQUEST, exception.getErrorCode());
    }

    @Test
    void cancelFriendRequestDeletesWhenRequesterAndPending() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        friendRequestService.cancelFriendRequest(1L, 100L);

        verify(friendRequestRepository).delete(friendRequest);
    }

    @Test
    void cancelFriendRequestThrowsWhenRequestNotFound() {
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> friendRequestService.cancelFriendRequest(1L, 100L)
        );

        assertEquals(ErrorCode.FRIEND_REQUEST_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void cancelFriendRequestThrowsWhenRequesterDoesNotMatch() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> friendRequestService.cancelFriendRequest(3L, 100L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(friendRequestRepository, never()).delete(friendRequest);
    }

    @Test
    void cancelFriendRequestThrowsWhenAlreadyClosed() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        friendRequest.accept();
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> friendRequestService.cancelFriendRequest(1L, 100L)
        );

        assertEquals(ErrorCode.FRIEND_REQUEST_CLOSED, exception.getErrorCode());
        verify(friendRequestRepository, never()).delete(friendRequest);
    }

    @Test
    void acceptFriendRequestReturnsFriendStatusWhenSuccess() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        FriendRequestAcceptResponse response = friendRequestService.acceptFriendRequest(2L, 100L);

        assertEquals(FriendStatus.FRIEND, response.friendStatus());
        assertNotNull(response.closedAt());
        assertEquals(FriendRequestStatus.ACCEPTED, friendRequest.getStatus());
    }

    @Test
    void acceptFriendRequestThrowsWhenReceiverDoesNotMatch() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> friendRequestService.acceptFriendRequest(3L, 100L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    @Test
    void rejectFriendRequestReturnsNoneStatusWhenSuccess() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        FriendRequestRejectResponse response = friendRequestService.rejectFriendRequest(2L, 100L);

        assertEquals(FriendStatus.NONE, response.friendStatus());
        assertNotNull(response.closedAt());
        assertEquals(FriendRequestStatus.REJECTED, friendRequest.getStatus());
    }

    @Test
    void rejectFriendRequestThrowsWhenReceiverDoesNotMatch() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> friendRequestService.rejectFriendRequest(3L, 100L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    // ── 이벤트 발행 검증 ──

    @Test
    void createFriendRequestPublishesEventToTargetUser() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        friendRequestService.createFriendRequest(1L, 2L);

        ArgumentCaptor<FriendRequestEvent> captor = ArgumentCaptor.forClass(FriendRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        FriendRequestEvent event = captor.getValue();
        assertEquals(2L, event.receiverId());
        assertEquals(NotificationType.FRIEND_REQUESTED, event.type());
        assertEquals(1L, event.data());
    }

    @Test
    void cancelFriendRequestPublishesEventToReceiver() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        friendRequestService.cancelFriendRequest(1L, 100L);

        ArgumentCaptor<FriendRequestEvent> captor = ArgumentCaptor.forClass(FriendRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        FriendRequestEvent event = captor.getValue();
        assertEquals(2L, event.receiverId());
        assertEquals(NotificationType.FRIEND_REQUEST_CANCELLED, event.type());
        assertEquals(1L, event.data());
    }

    @Test
    void acceptFriendRequestPublishesEventToRequester() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        friendRequestService.acceptFriendRequest(2L, 100L);

        ArgumentCaptor<FriendRequestEvent> captor = ArgumentCaptor.forClass(FriendRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        FriendRequestEvent event = captor.getValue();
        assertEquals(1L, event.receiverId());
        assertEquals(NotificationType.FRIEND_REQUEST_ACCEPTED, event.type());
        assertEquals(2L, event.data());
    }

    @Test
    void rejectFriendRequestPublishesEventToRequester() {
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findById(100L)).thenReturn(Optional.of(friendRequest));

        friendRequestService.rejectFriendRequest(2L, 100L);

        ArgumentCaptor<FriendRequestEvent> captor = ArgumentCaptor.forClass(FriendRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        FriendRequestEvent event = captor.getValue();
        assertEquals(1L, event.receiverId());
        assertEquals(NotificationType.FRIEND_REQUEST_REJECTED, event.type());
        assertEquals(2L, event.data());
    }

    @Test
    void createFriendRequestDoesNotPublishEventWhenUserNotFound() {
        when(userRepository.existsById(2L)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> friendRequestService.createFriendRequest(1L, 2L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createFriendRequestDoesNotPublishEventWhenDuplicate() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(BusinessException.class,
                () -> friendRequestService.createFriendRequest(1L, 2L));

        verify(eventPublisher, never()).publishEvent(any());
    }
}
