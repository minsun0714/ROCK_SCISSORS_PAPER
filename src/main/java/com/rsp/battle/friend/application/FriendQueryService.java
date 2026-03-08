package com.rsp.battle.friend.application;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.friend.presentation.FriendResponse;
import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.friendRequest.persistence.FriendRequestRepository;
import com.rsp.battle.user.domain.PresenceStatus;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.PresenceRepository;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.FriendStatus;
import com.rsp.battle.user.presentation.ProfileImageUrlResolver;
import com.rsp.battle.user.presentation.dto.response.FriendInfo;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FriendQueryService {

    private final UserRepository userRepository;
    private final PresenceRepository presenceRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final ProfileImageUrlResolver profileImageUrlResolver;

    public Paginated<FriendResponse> getMyPaginatedFriends(Long loginUserId, String keyword, Pageable pageable) {
        Page<User> page = userRepository.searchAcceptedFriendRequestByNickname(
                keyword,
                loginUserId,
                pageable
        );

        List<Long> idList = page.map(User::getId).getContent();

        Map<Long, PresenceStatus> presenceStatusMap = presenceRepository.getPresenceStatuses(idList);

        Page<FriendResponse> friendResponses = page.map(friend -> {
            String profileImageUrl = profileImageUrlResolver.resolve(friend.getProfileImageKey());
            PresenceStatus presenceStatus = presenceStatusMap.get(friend.getId());

            return new FriendResponse(
                    friend.getId(),
                    friend.getNickname(),
                    profileImageUrl,
                    friend.getStatusMessage(),
                    presenceStatus,
                    FriendInfo.of(FriendStatus.FRIEND, null)
            );
        });

        return Paginated.from(friendResponses);
    }

    public Paginated<FriendResponse> getOtherUserPaginatedFriends(CustomUserPrincipal loginUser, Long targetUserId, String keyword, Pageable pageable) {
        Page<User> page = userRepository.searchAcceptedFriendRequestByNickname(
                keyword,
                targetUserId,
                pageable
        );

        List<Long> idList = page.map(User::getId).getContent();

        Map<Long, PresenceStatus> presenceStatusMap = presenceRepository.getPresenceStatuses(idList);

        Map<Long, FriendInfo> friendInfoMap;

        if (loginUser != null) {
            friendInfoMap = friendRequestRepository
                    .findAllByUserIdPairIn(loginUser.getUserId(), idList)
                    .stream()
                    .collect(Collectors.toMap(
                            fr -> Objects.equals(fr.getRequester(), loginUser.getUserId())
                                    ? fr.getReceiver()
                                    : fr.getRequester(),
                            fr -> FriendInfo.of(
                                    FriendStatus.from(fr.getStatus(), loginUser.getUserId(), fr.getRequester()),
                                    fr.getId()
                            )
                    ));
        } else {
            friendInfoMap = new HashMap<>();
        }

        Page<FriendResponse> friendResponses = page.map(friend -> {
            String profileImageUrl = profileImageUrlResolver.resolve(friend.getProfileImageKey());
            PresenceStatus presenceStatus = presenceStatusMap.get(friend.getId());
            FriendInfo friendInfo = loginUser == null
                    ? FriendInfo.none()
                    : friendInfoMap.getOrDefault(friend.getId(), FriendInfo.none());

            return new FriendResponse(
                    friend.getId(),
                    friend.getNickname(),
                    profileImageUrl,
                    friend.getStatusMessage(),
                    presenceStatus,
                    friendInfo
            );
        });

        return Paginated.from(friendResponses);
    }
}
