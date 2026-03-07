package com.rsp.battle.user.persistence;

import com.rsp.battle.user.domain.PresenceStatus;

import java.util.List;
import java.util.Map;

public interface PresenceRepository {
    void setPresenceStatusOnline(Long userId);
    PresenceStatus getPresenceStatus(Long userId);
    Map<Long, PresenceStatus> getPresenceStatuses(List<Long> userIds);
}
