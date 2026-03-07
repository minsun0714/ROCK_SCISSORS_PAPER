package com.rsp.battle.user.presentation.dto.response;

import com.rsp.battle.user.domain.PresenceStatus;
import com.rsp.battle.user.presentation.FriendStatus;

public record UserSearchResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String statusMessage,
        PresenceStatus presenceStatus,
        FriendStatus friendStatus
) { }