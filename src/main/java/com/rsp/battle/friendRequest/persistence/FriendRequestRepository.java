package com.rsp.battle.friendRequest.persistence;

import com.rsp.battle.friendRequest.domain.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    // 두명의 유저의 친구 요청 상태를 조회
    FriendRequest findFirstByUserLowIdAndUserHighIdOrderByCreatedAtDesc(Long lowId, Long highId);
}
