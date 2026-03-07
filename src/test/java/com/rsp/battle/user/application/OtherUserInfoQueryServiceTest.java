package com.rsp.battle.user.application;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.friendRequest.persistence.FriendRequestRepository;
import com.rsp.battle.user.domain.PresenceStatus;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.PresenceRepository;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.FriendStatus;
import com.rsp.battle.user.presentation.OtherInfoResponse;
import com.rsp.battle.user.presentation.ProfileImageUrlResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtherUserInfoQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileImageUrlResolver profileImageUrlResolver;

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @InjectMocks
    private OtherUserInfoQueryService otherUserInfoQueryService;

    private User targetUser;
    private CustomUserPrincipal loginUser;

    private void setupCommon() {
        targetUser = User.builder()
                .id(2L)
                .email("target@example.com")
                .nickname("target")
                .profileImageKey("profile/target.png")
                .oauthProvider("GOOGLE")
                .build();
        loginUser = new CustomUserPrincipal(
                1L,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(profileImageUrlResolver.resolve("profile/target.png"))
                .thenReturn("https://cdn.example.com/profile/target.png");
        when(presenceRepository.getPresenceStatus(2L)).thenReturn(PresenceStatus.ONLINE);
    }

    @Test
    void getOtherUserInfo_returnsNoneWhenLoginUserIsNull() {
        targetUser = User.builder()
                .id(2L)
                .email("target@example.com")
                .nickname("target")
                .profileImageKey("profile/target.png")
                .oauthProvider("GOOGLE")
                .build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(profileImageUrlResolver.resolve("profile/target.png"))
                .thenReturn("https://cdn.example.com/profile/target.png");
        when(presenceRepository.getPresenceStatus(2L)).thenReturn(PresenceStatus.ONLINE);

        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(null, 2L);

        assertEquals(FriendStatus.NONE, response.friendStatus());
    }

    @Test
    void getOtherUserInfo_returnsNoneWhenNoFriendRequest() {
        setupCommon();
        when(friendRequestRepository.findFirstByUserLowIdAndUserHighIdOrderByCreatedAtDesc(1L, 2L))
                .thenReturn(null);

        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(loginUser, 2L);

        assertEquals(FriendStatus.NONE, response.friendStatus());
    }

    @Test
    void getOtherUserInfo_returnsFriendWhenAccepted() {
        setupCommon();
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        friendRequest.accept();
        when(friendRequestRepository.findFirstByUserLowIdAndUserHighIdOrderByCreatedAtDesc(1L, 2L))
                .thenReturn(friendRequest);

        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(loginUser, 2L);

        assertEquals(FriendStatus.FRIEND, response.friendStatus());
    }

    @Test
    void getOtherUserInfo_returnsRequestedWhenIRequested() {
        setupCommon();
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        when(friendRequestRepository.findFirstByUserLowIdAndUserHighIdOrderByCreatedAtDesc(1L, 2L))
                .thenReturn(friendRequest);

        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(loginUser, 2L);

        assertEquals(FriendStatus.REQUESTED, response.friendStatus());
    }

    @Test
    void getOtherUserInfo_returnsPendingWhenTargetRequested() {
        setupCommon();
        FriendRequest friendRequest = FriendRequest.create(2L, 1L);
        when(friendRequestRepository.findFirstByUserLowIdAndUserHighIdOrderByCreatedAtDesc(1L, 2L))
                .thenReturn(friendRequest);

        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(loginUser, 2L);

        assertEquals(FriendStatus.PENDING, response.friendStatus());
    }

    @Test
    void getOtherUserInfo_returnsNoneWhenRejected() {
        setupCommon();
        FriendRequest friendRequest = FriendRequest.create(1L, 2L);
        friendRequest.reject();
        when(friendRequestRepository.findFirstByUserLowIdAndUserHighIdOrderByCreatedAtDesc(1L, 2L))
                .thenReturn(friendRequest);

        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(loginUser, 2L);

        assertEquals(FriendStatus.NONE, response.friendStatus());
    }

    @Test
    void getOtherUserInfo_throwsWhenTargetUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otherUserInfoQueryService.getOtherUserInfo(null, 99L)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}
