package com.rsp.battle.battle.persistence;

public interface BattleStatProjection {
    Long getTotalCount();
    Long getWinCount();
    Long getLoseCount();
    Long getDrawCount();
}
