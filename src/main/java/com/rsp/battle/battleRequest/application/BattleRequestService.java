package com.rsp.battle.battleRequest.application;

import com.rsp.battle.battleRequest.domain.BattleRoom;
import com.rsp.battle.battleRequest.domain.BattleRoomStatus;
import com.rsp.battle.battleRequest.domain.event.BattleRequestEvent;
import com.rsp.battle.battleRequest.persistence.BattleRoomRepository;
import com.rsp.battle.battleRequest.presentation.BattleRequestAcceptResponse;
import com.rsp.battle.battleRequest.presentation.BattleRequestRejectResponse;
import com.rsp.battle.battleRequest.presentation.dto.response.BattleRequestResponse;
import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.notification.presentation.NotificationType;
import com.rsp.battle.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BattleRequestService {

    private final UserRepository userRepository;
    private final BattleRoomRepository battleRoomRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public BattleRequestResponse createBattleRequest(Long userId, Long targetUserId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        BattleRoom battleRoom = BattleRoom.create(
                userId,
                targetUserId
        );

        try {
            battleRoomRepository.save(battleRoom);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_BATTLE_REQUEST);
        }

        eventPublisher.publishEvent(
                new BattleRequestEvent(
                        targetUserId, NotificationType.BATTLE_REQUESTED, userId
                )
        );

        return BattleRequestResponse.from(battleRoom);
    }

    @Transactional
    public void cancelBattleRequest(Long userId, Long requestId) {
        BattleRoom battleRoom = battleRoomRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_CLOSED));

        if (!Objects.equals(battleRoom.getRequester(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (battleRoom.getStatus() != BattleRoomStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_CLOSED);
        }

        battleRoomRepository.delete(battleRoom);

        eventPublisher.publishEvent(
                new BattleRequestEvent(
                        battleRoom.getOpponent(), NotificationType.BATTLE_REQUEST_CANCELLED, userId
                )
        );
    }

    @Transactional
    public BattleRequestAcceptResponse acceptBattleRequest(Long userId, Long requestId) {
        BattleRoom battleRoom = battleRoomRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        if (!Objects.equals(battleRoom.getOpponent(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        battleRoom.startBattle();

        eventPublisher.publishEvent(
                new BattleRequestEvent(
                        battleRoom.getRequester(), NotificationType.BATTLE_REQUEST_ACCEPTED, userId
                )
        );

        return BattleRequestAcceptResponse.from(battleRoom);
    }

    @Transactional
    public BattleRequestRejectResponse rejectBattleRequest(Long userId, Long requestId) {
        BattleRoom battleRoom = battleRoomRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BATTLE_ROOM_NOT_FOUND));

        if (!Objects.equals(battleRoom.getOpponent(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        battleRoom.close();

        eventPublisher.publishEvent(
                new BattleRequestEvent(
                        battleRoom.getRequester(), NotificationType.BATTLE_REQUEST_REJECTED, userId
                )
        );

        return BattleRequestRejectResponse.from(battleRoom);
    }
}
