package com.rsp.battle.friend.presentation;

import com.rsp.battle.user.domain.PresenceStatus;
import com.rsp.battle.user.presentation.dto.response.FriendInfo;

public record FriendResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String statusMessage,
        PresenceStatus presenceStatus,
        FriendInfo friendInfo
) {
}