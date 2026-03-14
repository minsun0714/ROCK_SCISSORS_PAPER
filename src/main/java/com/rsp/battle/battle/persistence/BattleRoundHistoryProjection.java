package com.rsp.battle.battle.persistence;

import com.rsp.battle.battle.domain.Move;

import java.time.Instant;

public interface BattleRoundHistoryProjection {
    Long getId();
    Long getRequesterId();
    Long getOpponentId();
    String getNickName();
    String getProfileImageKey();
    Move getRequesterMove();
    Move getOpponentMove();
    Long getWinnerUserId();
    Instant getCreatedAt();
}