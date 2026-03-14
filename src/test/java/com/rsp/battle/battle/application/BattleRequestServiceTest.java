package com.rsp.battle.battle.application;

import com.rsp.battle.battle.domain.BattleRoom;
import com.rsp.battle.battle.domain.BattleRoomStatus;
import com.rsp.battle.battle.domain.event.BattleRequestEvent;
import com.rsp.battle.battle.persistence.BattleRoomRepository;
import com.rsp.battle.battle.presentation.dto.response.BattleRequestAcceptResponse;
import com.rsp.battle.battle.presentation.dto.response.BattleRequestRejectResponse;
import com.rsp.battle.battle.presentation.BattleStatus;
import com.rsp.battle.battle.presentation.dto.response.BattleRequestResponse;
import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.notification.presentation.NotificationType;
import com.rsp.battle.user.persistence.UserRepository;
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
class BattleRequestServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BattleRoomRepository battleRoomRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BattleRequestService battleRequestService;

    // ── 배틀 신청 ──

    @Test
    void createBattleRequestReturnsRequestedResponseWhenSuccess() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(battleRoomRepository.save(any(BattleRoom.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BattleRequestResponse response = battleRequestService.createBattleRequest(1L, 2L);

        assertEquals(BattleStatus.REQUESTED, response.battleStatus());
        assertEquals(2L, response.opponentId());
        verify(battleRoomRepository).save(any(BattleRoom.class));
    }

    @Test
    void createBattleRequestThrowsWhenTargetUserDoesNotExist() {
        when(userRepository.existsById(2L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.createBattleRequest(1L, 2L)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(battleRoomRepository, never()).save(any(BattleRoom.class));
    }

    @Test
    void createBattleRequestThrowsWhenDuplicateRequestExists() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(battleRoomRepository.save(any(BattleRoom.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.createBattleRequest(1L, 2L)
        );

        assertEquals(ErrorCode.DUPLICATE_BATTLE_REQUEST, exception.getErrorCode());
    }

    // ── 배틀 취소 ──

    @Test
    void cancelBattleRequestDeletesWhenRequesterAndRequested() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        battleRequestService.cancelBattleRequest(1L, 100L);

        verify(battleRoomRepository).delete(room);
    }

    @Test
    void cancelBattleRequestThrowsWhenRoomNotFound() {
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.cancelBattleRequest(1L, 100L)
        );

        assertEquals(ErrorCode.BATTLE_ROOM_CLOSED, exception.getErrorCode());
    }

    @Test
    void cancelBattleRequestThrowsWhenRequesterDoesNotMatch() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.cancelBattleRequest(3L, 100L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
        verify(battleRoomRepository, never()).delete(room);
    }

    @Test
    void cancelBattleRequestThrowsWhenNotInRequestedStatus() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        room.startBattle();
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.cancelBattleRequest(1L, 100L)
        );

        assertEquals(ErrorCode.BATTLE_ROOM_CLOSED, exception.getErrorCode());
        verify(battleRoomRepository, never()).delete(room);
    }

    // ── 배틀 수락 ──

    @Test
    void acceptBattleRequestReturnsInProgressResponseWhenSuccess() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        BattleRequestAcceptResponse response = battleRequestService.acceptBattleRequest(2L, 100L);

        assertEquals(BattleStatus.IN_PROGRESS, response.battleStatus());
        assertNotNull(response.startedAt());
        assertEquals(BattleRoomStatus.IN_PROGRESS, room.getStatus());
    }

    @Test
    void acceptBattleRequestThrowsWhenRoomNotFound() {
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.acceptBattleRequest(2L, 100L)
        );

        assertEquals(ErrorCode.BATTLE_ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void acceptBattleRequestThrowsWhenOpponentDoesNotMatch() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.acceptBattleRequest(3L, 100L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    // ── 배틀 거절 ──

    @Test
    void rejectBattleRequestReturnsRejectedResponseWhenSuccess() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        BattleRequestRejectResponse response = battleRequestService.rejectBattleRequest(2L, 100L);

        assertEquals(BattleStatus.REJECTED, response.battleStatus());
        assertNotNull(response.closedAt());
        assertEquals(BattleRoomStatus.CLOSED, room.getStatus());
    }

    @Test
    void rejectBattleRequestThrowsWhenRoomNotFound() {
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.rejectBattleRequest(2L, 100L)
        );

        assertEquals(ErrorCode.BATTLE_ROOM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void rejectBattleRequestThrowsWhenOpponentDoesNotMatch() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> battleRequestService.rejectBattleRequest(3L, 100L)
        );

        assertEquals(ErrorCode.FORBIDDEN, exception.getErrorCode());
    }

    // ── 이벤트 발행 검증 ──

    @Test
    void createBattleRequestPublishesEventToOpponent() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(battleRoomRepository.save(any(BattleRoom.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        battleRequestService.createBattleRequest(1L, 2L);

        ArgumentCaptor<BattleRequestEvent> captor = ArgumentCaptor.forClass(BattleRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        BattleRequestEvent event = captor.getValue();
        assertEquals(2L, event.receiverId());
        assertEquals(NotificationType.BATTLE_REQUESTED, event.type());
        assertEquals(1L, event.senderId());
    }

    @Test
    void cancelBattleRequestPublishesEventToOpponent() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        battleRequestService.cancelBattleRequest(1L, 100L);

        ArgumentCaptor<BattleRequestEvent> captor = ArgumentCaptor.forClass(BattleRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        BattleRequestEvent event = captor.getValue();
        assertEquals(2L, event.receiverId());
        assertEquals(NotificationType.BATTLE_REQUEST_CANCELLED, event.type());
        assertEquals(1L, event.senderId());
    }

    @Test
    void acceptBattleRequestPublishesEventToRequester() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        battleRequestService.acceptBattleRequest(2L, 100L);

        ArgumentCaptor<BattleRequestEvent> captor = ArgumentCaptor.forClass(BattleRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        BattleRequestEvent event = captor.getValue();
        assertEquals(1L, event.receiverId());
        assertEquals(NotificationType.BATTLE_REQUEST_ACCEPTED, event.type());
        assertEquals(2L, event.senderId());
    }

    @Test
    void rejectBattleRequestPublishesEventToRequester() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        when(battleRoomRepository.findById(100L)).thenReturn(Optional.of(room));

        battleRequestService.rejectBattleRequest(2L, 100L);

        ArgumentCaptor<BattleRequestEvent> captor = ArgumentCaptor.forClass(BattleRequestEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());

        BattleRequestEvent event = captor.getValue();
        assertEquals(1L, event.receiverId());
        assertEquals(NotificationType.BATTLE_REQUEST_REJECTED, event.type());
        assertEquals(2L, event.senderId());
    }

    @Test
    void createBattleRequestDoesNotPublishEventWhenUserNotFound() {
        when(userRepository.existsById(2L)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> battleRequestService.createBattleRequest(1L, 2L));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void createBattleRequestDoesNotPublishEventWhenDuplicate() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(battleRoomRepository.save(any(BattleRoom.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(BusinessException.class,
                () -> battleRequestService.createBattleRequest(1L, 2L));

        verify(eventPublisher, never()).publishEvent(any());
    }
}
