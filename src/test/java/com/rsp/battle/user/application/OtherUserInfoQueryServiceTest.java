package com.rsp.battle.user.application;

import com.rsp.battle.auth.domain.CustomUserPrincipal;
import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.FriendStatus;
import com.rsp.battle.user.presentation.OtherInfoResponse;
import com.rsp.battle.user.presentation.PresenceStatus;
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

    @InjectMocks
    private OtherUserInfoQueryService otherUserInfoQueryService;

    @Test
    void getOtherUserInfoReturnsFriendWhenLoginUserExists() {
        User targetUser = User.builder()
                .id(2L)
                .email("target@example.com")
                .nickname("target")
                .profileImageKey("profile/target.png")
                .oauthProvider("GOOGLE")
                .build();
        CustomUserPrincipal loginUser = new CustomUserPrincipal(
                1L,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(userRepository.findById(2L)).thenReturn(Optional.of(targetUser));
        when(profileImageUrlResolver.resolve("profile/target.png"))
                .thenReturn("https://cdn.example.com/profile/target.png");

        OtherInfoResponse response = otherUserInfoQueryService.getOtherUserInfo(loginUser, 2L);

        assertEquals(2L, response.userId());
        assertEquals("target", response.nickname());
        assertEquals("https://cdn.example.com/profile/target.png", response.profileImageKey());
    }

    @Test
    void getOtherUserInfoThrowsWhenTargetUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> otherUserInfoQueryService.getOtherUserInfo(null, 99L)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }
}
