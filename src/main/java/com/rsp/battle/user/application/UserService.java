package com.rsp.battle.user.application;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.UserProfileRequest;
import com.rsp.battle.user.presentation.UserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User createIfNotExists(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String nickname = oAuth2User.getAttribute("name");
        String OAUTH_PROVIDER = "GOOGLE";

        return userRepository.findActiveUserByEmailAndAuthProvider(email, "GOOGLE")
                .orElseGet(() -> {
                    User newUser = User.createFromOAuth(email, nickname, OAUTH_PROVIDER);
                    return userRepository.save(newUser);
                });
    }

    @Transactional
    public UserProfileResponse updateStatusMessage(long userId, UserProfileRequest userProfileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateStatusMessage(userProfileRequest.statusMessage());

        return UserProfileResponse.from(user);
    }
}