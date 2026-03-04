package com.rsp.battle.friendRequest.persistence;

import com.rsp.battle.friendRequest.domain.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
}
