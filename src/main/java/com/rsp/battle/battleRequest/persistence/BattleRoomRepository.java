package com.rsp.battle.battleRequest.persistence;

import com.rsp.battle.battleRequest.domain.BattleRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattleRoomRepository extends JpaRepository<BattleRoom, Long> {
}
