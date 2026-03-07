package com.rsp.battle.user.application;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.PresenceRepository;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.FriendStatus;
import com.rsp.battle.user.presentation.OtherInfoResponse;
import com.rsp.battle.user.presentation.PresenceStatus;
import com.rsp.battle.user.presentation.ProfileImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtherUserInfoQueryService {

    private final UserRepository userRepository;
    private final ProfileImageUrlResolver profileImageUrlResolver;
    private final PresenceRepository presenceRepository;

    public OtherInfoResponse getOtherUserInfo(CustomUserPrincipal loginUser, Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String profileImageUrl = profileImageUrlResolver.resolve(targetUser.getProfileImageKey());

        PresenceStatus presenceStatus = presenceRepository.getPresenceStatus(targetUserId);

        // 추후 실제값으로 변경 예정 (loginUserId가 null일 때는 NONE으로 처리)
        FriendStatus friendStatus = FriendStatus.NONE;
        if (loginUser != null) friendStatus = FriendStatus.FRIEND;

        return OtherInfoResponse.from(targetUser, profileImageUrl, presenceStatus, friendStatus);
    }
}
