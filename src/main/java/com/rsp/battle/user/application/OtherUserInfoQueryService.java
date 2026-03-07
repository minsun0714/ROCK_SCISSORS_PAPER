package com.rsp.battle.user.application;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.friendRequest.domain.FriendRequestStatus;
import com.rsp.battle.friendRequest.persistence.FriendRequestRepository;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.PresenceRepository;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.FriendStatus;
import com.rsp.battle.user.presentation.OtherInfoResponse;
import com.rsp.battle.user.domain.PresenceStatus;
import com.rsp.battle.user.presentation.ProfileImageUrlResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtherUserInfoQueryService {

    private final UserRepository userRepository;
    private final ProfileImageUrlResolver profileImageUrlResolver;
    private final PresenceRepository presenceRepository;
    private final FriendRequestRepository friendRequestRepository;

    public OtherInfoResponse getOtherUserInfo(CustomUserPrincipal loginUser, Long targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String profileImageUrl = profileImageUrlResolver.resolve(targetUser.getProfileImageKey());

        PresenceStatus presenceStatus = presenceRepository.getPresenceStatus(targetUserId);

        if (loginUser == null) {
            return OtherInfoResponse.from(targetUser, profileImageUrl, presenceStatus, FriendStatus.NONE);
        }

        FriendStatus friendStatus = resolveFriendStatus(loginUser.getUserId(), targetUserId);

        return OtherInfoResponse.from(targetUser, profileImageUrl, presenceStatus, friendStatus);
    }

    private FriendStatus resolveFriendStatus(Long loginUserId, Long targetUserId) {
        Long low = Math.min(loginUserId, targetUserId);
        Long high = Math.max(loginUserId, targetUserId);

        FriendRequest friendRequest = friendRequestRepository.findFirstByUserLowIdAndUserHighIdOrderByCreatedAtDesc(low, high);

        if (friendRequest == null) {
            return FriendStatus.NONE;
        }

        FriendRequestStatus friendRequestStatus = friendRequest.getStatus();
        return FriendStatus.from(friendRequestStatus, loginUserId, friendRequest.getRequester());
    }
}
