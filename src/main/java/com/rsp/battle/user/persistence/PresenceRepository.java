package com.rsp.battle.user.persistence;

import com.rsp.battle.user.presentation.PresenceStatus;

public interface PresenceRepository {
    void setPresenceStatusOnline(Long userId);
    PresenceStatus getPresenceStatus(Long userId);

}
