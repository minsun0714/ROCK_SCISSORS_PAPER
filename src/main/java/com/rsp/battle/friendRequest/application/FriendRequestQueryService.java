package com.rsp.battle.friendRequest.application;

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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FriendRequestQueryService {

    private final UserRepository userRepository;
    private final PresenceRepository presenceRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final ProfileImageUrlResolver profileImageUrlResolver;

    public Paginated<FriendResponse> getMyPaginatedPendingUserList(Long loginUserId, String keyword, Pageable pageable) {
        Page<User> page = userRepository.searchPendingFriendRequestByNickname(
                keyword,
                loginUserId,
                pageable
        );

        List<Long> idList = page.map(User::getId).getContent();

        Map<Long, PresenceStatus> presenceStatusMap = presenceRepository.getPresenceStatuses(idList);

        Map<Long, Long> friendRequestIdMap = friendRequestRepository
                .findAllByUserIdPairIn(loginUserId, idList)
                .stream()
                .collect(Collectors.toMap(
                        fr -> Objects.equals(fr.getRequester(), loginUserId)
                                ? fr.getReceiver()
                                : fr.getRequester(),
                        FriendRequest::getId
                ));

        Page<FriendResponse> friendResponses = page.map(friend -> {
            String profileImageUrl = profileImageUrlResolver.resolve(friend.getProfileImageKey());
            PresenceStatus presenceStatus = presenceStatusMap.get(friend.getId());

            return new FriendResponse(
                    friend.getId(),
                    friend.getNickname(),
                    profileImageUrl,
                    friend.getStatusMessage(),
                    presenceStatus,
                    FriendInfo.of(FriendStatus.PENDING, friendRequestIdMap.get(friend.getId()))
            );
        });

        return Paginated.from(friendResponses);
    }

    public Paginated<FriendResponse> getMyPaginatedRequestedUserList(Long loginUserId, String keyword, Pageable pageable) {
        Page<User> page = userRepository.searchRequestedFriendRequestByNickname(
                keyword,
                loginUserId,
                pageable
        );

        List<Long> idList = page.map(User::getId).getContent();

        Map<Long, PresenceStatus> presenceStatusMap = presenceRepository.getPresenceStatuses(idList);

        Map<Long, Long> friendRequestIdMap = friendRequestRepository
                .findAllByUserIdPairIn(loginUserId, idList)
                .stream()
                .collect(Collectors.toMap(
                        fr -> Objects.equals(fr.getRequester(), loginUserId)
                                ? fr.getReceiver()
                                : fr.getRequester(),
                        FriendRequest::getId
                ));

        Page<FriendResponse> friendResponses = page.map(friend -> {
            String profileImageUrl = profileImageUrlResolver.resolve(friend.getProfileImageKey());
            PresenceStatus presenceStatus = presenceStatusMap.get(friend.getId());

            return new FriendResponse(
                    friend.getId(),
                    friend.getNickname(),
                    profileImageUrl,
                    friend.getStatusMessage(),
                    presenceStatus,
                    FriendInfo.of(FriendStatus.REQUESTED, friendRequestIdMap.get(friend.getId()))
            );
        });

        return Paginated.from(friendResponses);
    }
}
