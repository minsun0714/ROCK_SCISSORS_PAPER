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
import com.rsp.battle.user.presentation.dto.response.Paginated;
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
class FriendQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private ProfileImageUrlResolver profileImageUrlResolver;

    @InjectMocks
    private FriendQueryService friendQueryService;

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

    // ===== getMyPaginatedFriends =====

    @Test
    void 내_친구_목록이_비어있으면_빈_페이지를_반환한다() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(userRepository.searchAcceptedFriendRequestByNickname("", 1L, pageable))
                .willReturn(emptyPage);
        given(presenceRepository.getPresenceStatuses(List.of())).willReturn(Map.of());

        Paginated<FriendResponse> result = friendQueryService.getMyPaginatedFriends(1L, "", pageable);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void 내_친구_목록의_friendStatus는_전부_FRIEND이다() {
        Pageable pageable = PageRequest.of(0, 10);
        User friend1 = user(2L, "친구1");
        User friend2 = user(3L, "친구2");
        Page<User> page = new PageImpl<>(List.of(friend1, friend2), pageable, 2);

        given(userRepository.searchAcceptedFriendRequestByNickname("", 1L, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L, 3L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE, 3L, PresenceStatus.OFFLINE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendQueryService.getMyPaginatedFriends(1L, "", pageable);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).allMatch(r -> r.friendInfo().status() == FriendStatus.FRIEND);
    }

    @Test
    void 내_친구_목록에서_presenceStatus가_정상_매핑된다() {
        Pageable pageable = PageRequest.of(0, 10);
        User friend1 = user(2L, "친구1");
        User friend2 = user(3L, "친구2");
        Page<User> page = new PageImpl<>(List.of(friend1, friend2), pageable, 2);

        given(userRepository.searchAcceptedFriendRequestByNickname("", 1L, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L, 3L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE, 3L, PresenceStatus.IN_BATTLE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendQueryService.getMyPaginatedFriends(1L, "", pageable);

        assertThat(result.content().get(0).presenceStatus()).isEqualTo(PresenceStatus.ONLINE);
        assertThat(result.content().get(1).presenceStatus()).isEqualTo(PresenceStatus.IN_BATTLE);
    }

    @Test
    void 내_친구_목록에서_키워드로_검색할_수_있다() {
        Pageable pageable = PageRequest.of(0, 10);
        User friend = user(2L, "LazyPanda");
        Page<User> page = new PageImpl<>(List.of(friend), pageable, 1);

        given(userRepository.searchAcceptedFriendRequestByNickname("Lazy", 1L, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendQueryService.getMyPaginatedFriends(1L, "Lazy", pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).nickname()).isEqualTo("LazyPanda");
    }

    // ===== getOtherUserPaginatedFriends =====

    @Test
    void 다른_유저의_친구_목록에서_나와의_관계가_표시된다() {
        Pageable pageable = PageRequest.of(0, 10);
        Long loginUserId = 1L;
        Long targetUserId = 5L;
        User otherFriend = user(2L, "공통친구");
        Page<User> page = new PageImpl<>(List.of(otherFriend), pageable, 1);

        FriendRequest friendRequest = FriendRequest.create(loginUserId, 2L);
        friendRequest.accept();

        given(userRepository.searchAcceptedFriendRequestByNickname("", targetUserId, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(friendRequestRepository.findAllByUserIdPairIn(loginUserId, List.of(2L)))
                .willReturn(List.of(friendRequest));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendQueryService.getOtherUserPaginatedFriends(
                loginUser(loginUserId), targetUserId, "", pageable);

        assertThat(result.content().get(0).friendInfo().status()).isEqualTo(FriendStatus.FRIEND);
    }

    @Test
    void 다른_유저의_친구_목록에서_관계가_없으면_NONE이다() {
        Pageable pageable = PageRequest.of(0, 10);
        Long loginUserId = 1L;
        Long targetUserId = 5L;
        User otherFriend = user(2L, "모르는사람");
        Page<User> page = new PageImpl<>(List.of(otherFriend), pageable, 1);

        given(userRepository.searchAcceptedFriendRequestByNickname("", targetUserId, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.OFFLINE));
        given(friendRequestRepository.findAllByUserIdPairIn(loginUserId, List.of(2L)))
                .willReturn(List.of());
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendQueryService.getOtherUserPaginatedFriends(
                loginUser(loginUserId), targetUserId, "", pageable);

        assertThat(result.content().get(0).friendInfo().status()).isEqualTo(FriendStatus.NONE);
    }

    @Test
    void 비로그인_상태에서_다른_유저의_친구_목록을_조회하면_전부_NONE이다() {
        Pageable pageable = PageRequest.of(0, 10);
        Long targetUserId = 5L;
        User otherFriend = user(2L, "누군가");
        Page<User> page = new PageImpl<>(List.of(otherFriend), pageable, 1);

        given(userRepository.searchAcceptedFriendRequestByNickname("", targetUserId, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendQueryService.getOtherUserPaginatedFriends(
                null, targetUserId, "", pageable);

        assertThat(result.content().get(0).friendInfo().status()).isEqualTo(FriendStatus.NONE);
    }
}
