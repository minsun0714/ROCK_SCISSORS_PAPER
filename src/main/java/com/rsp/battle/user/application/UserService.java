package com.rsp.battle.user.application;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.user.domain.User;
import com.rsp.battle.user.persistence.ProfileImageRepository;
import com.rsp.battle.user.persistence.UserRepository;
import com.rsp.battle.user.presentation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ProfileImageUrlResolver profileImageUrlResolver;

    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

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

    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(long userId) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // profileImageUrl은 profileImgKey를 이용하여 생성
        String profileImageUrl = profileImageUrlResolver.resolve(me.getProfileImageKey());

        // presenceStatus는 추후 레디스로 구현 예정
        PresenceStatus presenceStatus = PresenceStatus.ONLINE;
        return MyInfoResponse.from(me, profileImageUrl, presenceStatus);
    }

    @Transactional
    public StatusMessageUpdateResponse updateStatusMessage(long userId, StatusMessageUpdateRequest statusMessageUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateStatusMessage(statusMessageUpdateRequest.statusMessage());

        return StatusMessageUpdateResponse.from(user);
    }

    public ProfilePresignedUrlResponse createProfilePicture(long userId, ProfilePresignedUrlRequest profilePresignedUrlRequest) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!ALLOWED_TYPES.contains(profilePresignedUrlRequest.fileType())) {
            throw new BusinessException(ErrorCode.INVALID_FILE_TYPE);
        }

        return profileImageRepository.createUploadUrl(
                profilePresignedUrlRequest.fileName(),
                profilePresignedUrlRequest.fileType()
        );
    }

    @Transactional
    public void updateProfilePictureKey(Long userId, ProfilePictureUpdateRequest profilePictureUpdateRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.updateProfileImageKey(profilePictureUpdateRequest.key());
    }
}