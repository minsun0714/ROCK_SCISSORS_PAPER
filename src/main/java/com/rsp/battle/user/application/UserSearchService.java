package com.rsp.battle.user.application;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.friendRequest.domain.FriendRequestStatus;
import com.rsp.battle.friendRequest.persistence.FriendRequestRepository;
import com.rsp.battle.user.domain.PresenceStatus;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.PresenceRepository;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.FriendStatus;
import com.rsp.battle.user.presentation.ProfileImageUrlResolver;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import com.rsp.battle.user.presentation.dto.response.UserSearchResponse;
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
public class UserSearchService {

    private final UserRepository userRepository;
    private final PresenceRepository presenceRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final ProfileImageUrlResolver profileImageUrlResolver;

    @Transactional(readOnly = true)
    public Paginated<UserSearchResponse> searchUsers(CustomUserPrincipal loginUser, String keyword, Pageable pageable) {
        /**
         *   아래 FULL TEXT 인덱스 필요
         *   ALTER TABLE users ADD FULLTEXT INDEX ft_users_nickname (nickname) WITH PARSER ngram;
         */
        Page<User> page = userRepository.searchByNickname(keyword, pageable);

        List<Long> idList = page.map(User::getId).getContent();

        Map<Long, PresenceStatus> presenceStatusMap = presenceRepository.getPresenceStatuses(idList);

        Map<Long, FriendStatus> friendStatusMap;

        if (loginUser != null) {
            friendStatusMap = friendRequestRepository
                .findAllByUserIdPairIn(loginUser.getUserId(), idList)
                .stream()
                .collect(Collectors.toMap(
        fr -> Objects.equals(fr.getRequester(), loginUser.getUserId())
                        ? fr.getReceiver()
                        : fr.getRequester(),
        fr -> FriendStatus.from(fr.getStatus(), loginUser.getUserId(), fr.getRequester())
                ));
        } else {
            friendStatusMap = new HashMap<>();
        }

        Page<UserSearchResponse> userSearchResponses = page.map(user -> {

            String profileImageUrl = profileImageUrlResolver.resolve(user.getProfileImageKey());
            PresenceStatus presenceStatus = presenceStatusMap.get(user.getId());
            FriendStatus friendStatus = loginUser == null
                    ? FriendStatus.NONE
                    : friendStatusMap.getOrDefault(user.getId(), FriendStatus.NONE);

            return new UserSearchResponse(
                    user.getId(),
                    user.getNickname(),
                    profileImageUrl,
                    user.getStatusMessage(),
                    presenceStatus,
                    friendStatus
            );
        });

        return Paginated.from(userSearchResponses);
    }
}
