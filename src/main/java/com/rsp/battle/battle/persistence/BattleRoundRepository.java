package com.rsp.battle.battle.persistence;

import com.rsp.battle.battle.domain.BattleRound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattleRoundRepository extends JpaRepository<BattleRound, Long> {

    BattleRound findFirstByRoomIdOrderByRoundNumberDesc(Long roomId);
}
