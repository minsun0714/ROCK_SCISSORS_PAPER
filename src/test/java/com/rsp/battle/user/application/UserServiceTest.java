package com.rsp.battle.user.application;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.PresenceRepository;
import com.rsp.battle.user.persistence.ProfileImageRepository;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.MyInfoResponse;
import com.rsp.battle.user.presentation.ProfileImageUrlResolver;
import com.rsp.battle.user.presentation.PresenceStatus;
import com.rsp.battle.user.presentation.ProfilePictureUpdateRequest;
import com.rsp.battle.user.presentation.ProfilePresignedUrlRequest;
import com.rsp.battle.user.presentation.ProfilePresignedUrlResponse;
import com.rsp.battle.user.presentation.StatusMessageUpdateRequest;
import com.rsp.battle.user.presentation.StatusMessageUpdateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileImageRepository profileImageRepository;

    @Mock
    private ProfileImageUrlResolver profileImageUrlResolver;

    @Mock
    private PresenceRepository presenceRepository;

    @Mock
    private OAuth2User oAuth2User;

    @InjectMocks
    private UserService userService;

    @Test
    void createIfNotExists() {
        User existingUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("tester")
                .oauthProvider("GOOGLE")
                .build();

        when(oAuth2User.getAttribute("email")).thenReturn("user@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("tester");
        when(userRepository.findActiveUserByEmailAndAuthProvider("user@example.com", "GOOGLE"))
                .thenReturn(Optional.of(existingUser));

        User result = userService.createIfNotExists(oAuth2User);

        assertEquals(existingUser, result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateStatusMessage() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("tester")
                .oauthProvider("GOOGLE")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        StatusMessageUpdateResponse result = userService.updateStatusMessage(1L, new StatusMessageUpdateRequest("new status"));

        assertEquals(1L, result.id());
        assertEquals("tester", result.nickname());
        assertEquals("new status", result.statusMessage());
        assertEquals("new status", user.getStatusMessage());
    }

    @Test
    void createIfNotExists_savesWhenNoUserExists() {
        when(oAuth2User.getAttribute("email")).thenReturn("new@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("newbie");
        when(userRepository.findActiveUserByEmailAndAuthProvider("new@example.com", "GOOGLE"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createIfNotExists(oAuth2User);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals("newbie", savedUser.getNickname());
        assertEquals("GOOGLE", savedUser.getOauthProvider());
        assertEquals(savedUser, result);
    }

    @Test
    void updateStatusMessage_throwsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateStatusMessage(99L, new StatusMessageUpdateRequest("status"))
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getMyInfo_returnsResolvedProfileImageUrl() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("tester")
                .profileImageKey("profile/user1.png")
                .statusMessage("hello")
                .oauthProvider("GOOGLE")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(profileImageUrlResolver.resolve("profile/user1.png"))
                .thenReturn("https://cdn.example.com/profile/user1.png");
        when(presenceRepository.getPresenceStatus(1L)).thenReturn(PresenceStatus.ONLINE);

        MyInfoResponse response = userService.getMyInfo(1L);

        assertEquals(1L, response.userId());
        assertEquals("tester", response.nickname());
        assertEquals("user@example.com", response.email());
        assertEquals("https://cdn.example.com/profile/user1.png", response.profileImageUrl());
        assertEquals("hello", response.statusMessage());
        assertEquals(PresenceStatus.ONLINE, response.presenceStatus());
    }

    @Test
    void getMyInfo_throwsWhenUserMissing() {
        when(userRepository.findById(77L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.getMyInfo(77L)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void createProfilePicture_returnsPresignedUrl() {
        ProfilePresignedUrlRequest request = new ProfilePresignedUrlRequest("cat.png", "image/png");
        ProfilePresignedUrlResponse expected = new ProfilePresignedUrlResponse(
                "https://s3.example.com/upload",
                "profile/uuid_cat.png"
        );

        when(userRepository.existsById(1L)).thenReturn(true);
        when(profileImageRepository.createUploadUrl("cat.png", "image/png")).thenReturn(expected);

        ProfilePresignedUrlResponse response = userService.createProfilePicture(1L, request);

        assertEquals(expected, response);
    }

    @Test
    void createProfilePicture_throwsWhenUserMissing() {
        ProfilePresignedUrlRequest request = new ProfilePresignedUrlRequest("cat.png", "image/png");
        when(userRepository.existsById(9L)).thenReturn(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createProfilePicture(9L, request)
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(profileImageRepository, never()).createUploadUrl(any(), any());
    }

    @Test
    void createProfilePicture_throwsWhenFileTypeIsInvalid() {
        ProfilePresignedUrlRequest request = new ProfilePresignedUrlRequest("cat.gif", "image/gif");
        when(userRepository.existsById(1L)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.createProfilePicture(1L, request)
        );

        assertEquals(ErrorCode.INVALID_FILE_TYPE, exception.getErrorCode());
        verify(profileImageRepository, never()).createUploadUrl(any(), any());
    }

    @Test
    void updateProfilePictureKey_updatesUserKey() {
        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .nickname("tester")
                .oauthProvider("GOOGLE")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updateProfilePictureKey(1L, new ProfilePictureUpdateRequest("profile/new.png"));

        assertEquals("profile/new.png", user.getProfileImageKey());
    }

    @Test
    void updateProfilePictureKey_throwsWhenUserMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> userService.updateProfilePictureKey(99L, new ProfilePictureUpdateRequest("profile/new.png"))
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void heartbeat_callsSetPresenceStatusOnline() {
        userService.heartbeat(1L);

        verify(presenceRepository).setPresenceStatusOnline(1L);
    }
}
