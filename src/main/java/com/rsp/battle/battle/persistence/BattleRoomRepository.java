package com.rsp.battle.battle.persistence;

import com.rsp.battle.battle.domain.BattleRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface BattleRoomRepository extends JpaRepository<BattleRoom, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select br from BattleRoom br where br.id = :roomId")
    BattleRoom findByIdForUpdate(Long roomId);
}
