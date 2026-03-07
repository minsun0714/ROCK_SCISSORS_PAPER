package com.rsp.battle.user.application;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.friendRequest.persistence.FriendRequestRepository;
import com.rsp.battle.user.domain.PresenceStatus;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.PresenceRepository;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.FriendStatus;
import com.rsp.battle.user.presentation.ProfileImageUrlResolver;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import com.rsp.battle.user.presentation.dto.response.UserSearchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserSearchServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private ProfileImageUrlResolver profileImageUrlResolver;

    @InjectMocks
    private UserSearchService userSearchService;

    private static CustomUserPrincipal loginUser(Long userId) {
        return new CustomUserPrincipal(userId, Collections.emptyList());
    }

    private static User user(Long id, String nickname) {
        return User.builder()
                .id(id)
                .nickname(nickname)
                .profileImageKey("img-" + id)
                .statusMessage("hello")
                .build();
    }

    @Test
    void 검색_결과가_없으면_빈_페이지를_반환한다() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(userRepository.searchByNickname("없는유저", pageable)).willReturn(emptyPage);
        given(presenceRepository.getPresenceStatuses(List.of())).willReturn(Map.of());
        given(friendRequestRepository.findAllByUserIdPairIn(1L, List.of())).willReturn(List.of());

        Paginated<UserSearchResponse> result = userSearchService.searchUsers(loginUser(1L), "없는유저", pageable);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void 로그인_유저가_null이면_friendStatus가_전부_NONE이다() {
        Pageable pageable = PageRequest.of(0, 10);
        User targetUser = user(2L, "com유저");
        Page<User> page = new PageImpl<>(List.of(targetUser), pageable, 1);

        given(userRepository.searchByNickname("com", pageable)).willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(profileImageUrlResolver.resolve("img-2")).willReturn("https://s3/img-2");

        Paginated<UserSearchResponse> result = userSearchService.searchUsers(null, "com", pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).friendStatus()).isEqualTo(FriendStatus.NONE);
    }

    @Test
    void 친구_요청을_보낸_유저는_REQUESTED로_표시된다() {
        Pageable pageable = PageRequest.of(0, 10);
        Long loginUserId = 1L;
        User targetUser = user(2L, "com유저");
        Page<User> page = new PageImpl<>(List.of(targetUser), pageable, 1);

        FriendRequest friendRequest = FriendRequest.create(loginUserId, 2L);

        given(userRepository.searchByNickname("com", pageable)).willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(friendRequestRepository.findAllByUserIdPairIn(loginUserId, List.of(2L)))
                .willReturn(List.of(friendRequest));
        given(profileImageUrlResolver.resolve("img-2")).willReturn("https://s3/img-2");

        Paginated<UserSearchResponse> result = userSearchService.searchUsers(loginUser(loginUserId), "com", pageable);

        assertThat(result.content().get(0).friendStatus()).isEqualTo(FriendStatus.REQUESTED);
    }

    @Test
    void 친구_요청을_받은_유저는_PENDING으로_표시된다() {
        Pageable pageable = PageRequest.of(0, 10);
        Long loginUserId = 1L;
        User targetUser = user(2L, "com유저");
        Page<User> page = new PageImpl<>(List.of(targetUser), pageable, 1);

        // targetUser(2L)가 요청을 보낸 경우
        FriendRequest friendRequest = FriendRequest.create(2L, loginUserId);

        given(userRepository.searchByNickname("com", pageable)).willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(friendRequestRepository.findAllByUserIdPairIn(loginUserId, List.of(2L)))
                .willReturn(List.of(friendRequest));
        given(profileImageUrlResolver.resolve("img-2")).willReturn("https://s3/img-2");

        Paginated<UserSearchResponse> result = userSearchService.searchUsers(loginUser(loginUserId), "com", pageable);

        assertThat(result.content().get(0).friendStatus()).isEqualTo(FriendStatus.PENDING);
    }

    @Test
    void 친구인_유저는_FRIEND로_표시된다() {
        Pageable pageable = PageRequest.of(0, 10);
        Long loginUserId = 1L;
        User targetUser = user(2L, "com유저");
        Page<User> page = new PageImpl<>(List.of(targetUser), pageable, 1);

        FriendRequest friendRequest = FriendRequest.create(loginUserId, 2L);
        friendRequest.accept();

        given(userRepository.searchByNickname("com", pageable)).willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(friendRequestRepository.findAllByUserIdPairIn(loginUserId, List.of(2L)))
                .willReturn(List.of(friendRequest));
        given(profileImageUrlResolver.resolve("img-2")).willReturn("https://s3/img-2");

        Paginated<UserSearchResponse> result = userSearchService.searchUsers(loginUser(loginUserId), "com", pageable);

        assertThat(result.content().get(0).friendStatus()).isEqualTo(FriendStatus.FRIEND);
    }

    @Test
    void 친구_요청이_없는_유저는_NONE으로_표시된다() {
        Pageable pageable = PageRequest.of(0, 10);
        Long loginUserId = 1L;
        User targetUser = user(2L, "com유저");
        Page<User> page = new PageImpl<>(List.of(targetUser), pageable, 1);

        given(userRepository.searchByNickname("com", pageable)).willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(friendRequestRepository.findAllByUserIdPairIn(loginUserId, List.of(2L)))
                .willReturn(List.of());
        given(profileImageUrlResolver.resolve("img-2")).willReturn("https://s3/img-2");

        Paginated<UserSearchResponse> result = userSearchService.searchUsers(loginUser(loginUserId), "com", pageable);

        assertThat(result.content().get(0).friendStatus()).isEqualTo(FriendStatus.NONE);
    }

    @Test
    void presenceStatus가_정상적으로_매핑된다() {
        Pageable pageable = PageRequest.of(0, 10);
        Long loginUserId = 1L;
        User user1 = user(2L, "com유저1");
        User user2 = user(3L, "com유저2");
        Page<User> page = new PageImpl<>(List.of(user1, user2), pageable, 2);

        given(userRepository.searchByNickname("com", pageable)).willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L, 3L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE, 3L, PresenceStatus.OFFLINE));
        given(friendRequestRepository.findAllByUserIdPairIn(loginUserId, List.of(2L, 3L)))
                .willReturn(List.of());
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<UserSearchResponse> result = userSearchService.searchUsers(loginUser(loginUserId), "com", pageable);

        assertThat(result.content().get(0).presenceStatus()).isEqualTo(PresenceStatus.ONLINE);
        assertThat(result.content().get(1).presenceStatus()).isEqualTo(PresenceStatus.OFFLINE);
    }
}