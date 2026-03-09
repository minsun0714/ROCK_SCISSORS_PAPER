package com.rsp.battle.battleRequest.domain;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BattleRoomTest {

    @Test
    void createBuildsRequestedRoomWithOrderedPairIds() {
        BattleRoom room = BattleRoom.create(10L, 2L);

        assertEquals(10L, room.getRequester());
        assertEquals(2L, room.getOpponent());
        assertEquals(2L, room.getUserLowId());
        assertEquals(10L, room.getUserHighId());
        assertEquals(BattleRoomStatus.REQUESTED, room.getStatus());
    }

    @Test
    void createThrowsWhenRequesterAndOpponentAreSame() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> BattleRoom.create(3L, 3L)
        );

        assertEquals(ErrorCode.SELF_BATTLE_REQUEST_NOT_ALLOWED, exception.getErrorCode());
    }

    @Test
    void startBattleUpdatesStatusToInProgress() {
        BattleRoom room = BattleRoom.create(1L, 2L);

        room.startBattle();

        assertEquals(BattleRoomStatus.IN_PROGRESS, room.getStatus());
    }

    @Test
    void startBattleThrowsWhenNotInRequestedStatus() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        room.close();

        BusinessException exception = assertThrows(
                BusinessException.class,
                room::startBattle
        );

        assertEquals(ErrorCode.BATTLE_ROOM_CLOSED, exception.getErrorCode());
    }

    @Test
    void closeUpdatesStatusAndClosedAt() {
        BattleRoom room = BattleRoom.create(1L, 2L);

        room.close();

        assertEquals(BattleRoomStatus.CLOSED, room.getStatus());
        assertNull(room.getActiveFlag());
    }

    @Test
    void closeThrowsWhenAlreadyClosed() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        room.close();

        BusinessException exception = assertThrows(
                BusinessException.class,
                room::close
        );

        assertEquals(ErrorCode.BATTLE_ROOM_CLOSED, exception.getErrorCode());
    }

    @Test
    void closeWorksWhenInProgress() {
        BattleRoom room = BattleRoom.create(1L, 2L);
        room.startBattle();

        room.close();

        assertEquals(BattleRoomStatus.CLOSED, room.getStatus());
        assertNull(room.getActiveFlag());
    }
}
