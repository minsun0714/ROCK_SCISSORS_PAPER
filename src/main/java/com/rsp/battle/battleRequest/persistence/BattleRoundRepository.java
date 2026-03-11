package com.rsp.battle.battleRequest.persistence;

import com.rsp.battle.battleRequest.domain.BattleRound;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BattleRoundRepository extends JpaRepository<BattleRound, Long> {

    BattleRound findFirstByRoomIdOrderByRoundNumberDesc(Long roomId);
}
