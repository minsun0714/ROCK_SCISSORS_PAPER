package com.rsp.battle.friendRequest.application;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import com.rsp.battle.friendRequest.domain.FriendRequest;
import com.rsp.battle.friendRequest.domain.FriendRequestStatus;
import com.rsp.battle.friendRequest.persistence.FriendRequestRepository;
import com.rsp.battle.friendRequest.presentation.FriendRequestAcceptResponse;
import com.rsp.battle.friendRequest.presentation.FriendRequestRejectResponse;
import com.rsp.battle.friendRequest.presentation.dto.response.FriendRequestResponse;
import com.rsp.battle.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendRequestService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;

    @Transactional
    public FriendRequestResponse createFriendRequest(Long userId, Long targetUserId) {
        if (!userRepository.existsById(targetUserId)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        FriendRequest friendRequest = FriendRequest.create(
                userId,
                targetUserId
        );

        try {
            friendRequestRepository.save(friendRequest);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_FRIEND_REQUEST);
        }

        return FriendRequestResponse.from(friendRequest);
    }

    @Transactional
    public void cancelFriendRequest(Long userId, Long requestId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!Objects.equals(friendRequest.getRequester(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (friendRequest.getStatus() != FriendRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_CLOSED);
        }

        friendRequestRepository.delete(friendRequest);
    }

    @Transactional
    public FriendRequestAcceptResponse acceptFriendRequest(Long userId, Long requestId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!Objects.equals(friendRequest.getReceiver(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        friendRequest.accept();

        return FriendRequestAcceptResponse.from(friendRequest);
    }

    @Transactional
    public FriendRequestRejectResponse rejectFriendRequest(Long userId, Long requestId) {
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!Objects.equals(friendRequest.getReceiver(), userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        friendRequest.reject();

        return FriendRequestRejectResponse.from(friendRequest);
    }
}
