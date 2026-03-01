package com.rsp.battle.user;

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
}