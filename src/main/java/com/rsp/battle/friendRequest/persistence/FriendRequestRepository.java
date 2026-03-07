package com.rsp.battle.friendRequest.persistence;

import com.rsp.battle.friendRequest.domain.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    // 두명의 유저의 친구 요청 상태를 조회
    FriendRequest findFirstByUserLowIdAndUserHighIdOrderByCreatedAtDesc(Long lowId, Long highId);

    @Query("""
            SELECT fr FROM FriendRequest fr
            WHERE
            fr.activeFlag IS NOT NULL
            AND (
                (fr.requester = :loginUserId AND fr.receiver IN :targetIdList)
                OR
                (fr.receiver = :loginUserId AND fr.requester IN :targetIdList)
            )
            """)
    List<FriendRequest> findAllByUserIdPairIn(
            @Param("loginUserId") Long loginUserId,
            @Param("targetIdList") List<Long> targetIdList);
}
