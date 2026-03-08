package com.rsp.battle.friendRequest.application;

import com.rsp.battle.friend.presentation.FriendResponse;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FriendRequestQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private ProfileImageUrlResolver profileImageUrlResolver;

    @InjectMocks
    private FriendRequestQueryService friendRequestQueryService;

    private static User user(Long id, String nickname) {
        return User.builder()
                .id(id)
                .nickname(nickname)
                .profileImageKey("img-" + id)
                .statusMessage("hello")
                .build();
    }

    // ===== getMyPaginatedPendingUserList (받은 요청) =====

    @Test
    void 받은_요청이_없으면_빈_페이지를_반환한다() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(userRepository.searchPendingFriendRequestByNickname("", 1L, pageable))
                .willReturn(emptyPage);
        given(presenceRepository.getPresenceStatuses(List.of())).willReturn(Map.of());

        Paginated<FriendResponse> result = friendRequestQueryService.getMyPaginatedPendingUserList(1L, "", pageable);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void 받은_요청_목록의_friendStatus는_전부_PENDING이다() {
        Pageable pageable = PageRequest.of(0, 10);
        User requester1 = user(2L, "요청자1");
        User requester2 = user(3L, "요청자2");
        Page<User> page = new PageImpl<>(List.of(requester1, requester2), pageable, 2);

        given(userRepository.searchPendingFriendRequestByNickname("", 1L, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L, 3L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE, 3L, PresenceStatus.OFFLINE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendRequestQueryService.getMyPaginatedPendingUserList(1L, "", pageable);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).allMatch(r -> r.friendStatus() == FriendStatus.PENDING);
    }

    @Test
    void 받은_요청_목록에서_키워드로_검색할_수_있다() {
        Pageable pageable = PageRequest.of(0, 10);
        User requester = user(2L, "LazyPanda");
        Page<User> page = new PageImpl<>(List.of(requester), pageable, 1);

        given(userRepository.searchPendingFriendRequestByNickname("Lazy", 1L, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendRequestQueryService.getMyPaginatedPendingUserList(1L, "Lazy", pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).nickname()).isEqualTo("LazyPanda");
    }

    @Test
    void 받은_요청_목록에서_presenceStatus가_정상_매핑된다() {
        Pageable pageable = PageRequest.of(0, 10);
        User requester = user(2L, "요청자");
        Page<User> page = new PageImpl<>(List.of(requester), pageable, 1);

        given(userRepository.searchPendingFriendRequestByNickname("", 1L, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.IN_BATTLE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendRequestQueryService.getMyPaginatedPendingUserList(1L, "", pageable);

        assertThat(result.content().get(0).presenceStatus()).isEqualTo(PresenceStatus.IN_BATTLE);
    }

    // ===== getMyPaginatedRequestedUserList (보낸 요청) =====

    @Test
    void 보낸_요청이_없으면_빈_페이지를_반환한다() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        given(userRepository.searchRequestedFriendRequestByNickname("", 1L, pageable))
                .willReturn(emptyPage);
        given(presenceRepository.getPresenceStatuses(List.of())).willReturn(Map.of());

        Paginated<FriendResponse> result = friendRequestQueryService.getMyPaginatedRequestedUserList(1L, "", pageable);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
    }

    @Test
    void 보낸_요청_목록의_friendStatus는_전부_REQUESTED이다() {
        Pageable pageable = PageRequest.of(0, 10);
        User receiver1 = user(2L, "대상자1");
        User receiver2 = user(3L, "대상자2");
        Page<User> page = new PageImpl<>(List.of(receiver1, receiver2), pageable, 2);

        given(userRepository.searchRequestedFriendRequestByNickname("", 1L, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L, 3L)))
                .willReturn(Map.of(2L, PresenceStatus.OFFLINE, 3L, PresenceStatus.ONLINE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendRequestQueryService.getMyPaginatedRequestedUserList(1L, "", pageable);

        assertThat(result.content()).hasSize(2);
        assertThat(result.content()).allMatch(r -> r.friendStatus() == FriendStatus.REQUESTED);
    }

    @Test
    void 보낸_요청_목록에서_키워드로_검색할_수_있다() {
        Pageable pageable = PageRequest.of(0, 10);
        User receiver = user(2L, "CoolCat");
        Page<User> page = new PageImpl<>(List.of(receiver), pageable, 1);

        given(userRepository.searchRequestedFriendRequestByNickname("Cool", 1L, pageable))
                .willReturn(page);
        given(presenceRepository.getPresenceStatuses(List.of(2L)))
                .willReturn(Map.of(2L, PresenceStatus.ONLINE));
        given(profileImageUrlResolver.resolve(anyString())).willReturn("https://s3/img");

        Paginated<FriendResponse> result = friendRequestQueryService.getMyPaginatedRequestedUserList(1L, "Cool", pageable);

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).nickname()).isEqualTo("CoolCat");
    }
}
