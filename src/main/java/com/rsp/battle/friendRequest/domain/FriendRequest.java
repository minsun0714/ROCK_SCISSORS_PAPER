package com.rsp.battle.friendRequest.domain;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "friend_request",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_friend_request_active_pair",
                        columnNames = {"user_low_id", "user_high_id", "active_flag"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_id", nullable = false)
    private Long requester;

    @Column(name = "receiver_id", nullable = false)
    private Long receiver;

    @Column(name = "user_low_id", nullable = false)
    private Long userLowId;

    @Column(name = "user_high_id", nullable = false)
    private Long userHighId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FriendRequestStatus status;

    /**
     * generated column (STORED)
     * insert/update 불가
     */
    @Column(name = "active_flag", insertable = false, updatable = false,
            columnDefinition = "TINYINT GENERATED ALWAYS AS (CASE WHEN status IN ('PENDING', 'ACCEPTED') THEN 1 ELSE NULL END) STORED")
    private Integer activeFlag;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    @Column(name = "closed_at")
    private Instant closedAt;

    public static FriendRequest create(Long requesterId, Long requestedId) {

        if (requesterId.equals(requestedId)) {
            throw new BusinessException(ErrorCode.SELF_FRIEND_REQUEST_NOT_ALLOWED);
        }

        Long low = Math.min(requesterId, requestedId);
        Long high = Math.max(requesterId, requestedId);

        return FriendRequest.builder()
                .requester(requesterId)
                .receiver(requestedId)
                .userLowId(low)
                .userHighId(high)
                .status(FriendRequestStatus.PENDING)
                .build();
    }

    public void accept() {
        if (status != FriendRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_CLOSED);
        }
        status = FriendRequestStatus.ACCEPTED;
        closedAt = Instant.now();
    }

    public void reject() {
        if (status != FriendRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.FRIEND_REQUEST_CLOSED);
        }
        status = FriendRequestStatus.REJECTED;
        closedAt = Instant.now();
    }
}