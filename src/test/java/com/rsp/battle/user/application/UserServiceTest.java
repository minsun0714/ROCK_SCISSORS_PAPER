package com.rsp.battle.user.application;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.UserProfileRequest;
import com.rsp.battle.user.presentation.UserProfileResponse;
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

        UserProfileResponse result = userService.updateStatusMessage(1L, new UserProfileRequest("new status"));

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
                () -> userService.updateStatusMessage(99L, new UserProfileRequest("status"))
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(userRepository, never()).save(any(User.class));
    }
}
