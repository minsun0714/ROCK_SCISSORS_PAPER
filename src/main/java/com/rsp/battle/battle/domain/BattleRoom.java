package com.rsp.battle.battle.domain;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "battle_room",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_battle_room_request_active_pair",
                        columnNames = {"user_low_id", "user_high_id", "active_flag"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BattleRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "latest_round_number", nullable = false)
    private Long latestRoundNumber;

    @Column(name = "requester_id", nullable = false)
    private Long requester;

    @Column(name = "opponent_id", nullable = false)
    private Long opponent;

    @Column(name = "user_low_id", nullable = false)
    private Long userLowId;

    @Column(name = "user_high_id", nullable = false)
    private Long userHighId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BattleRoomStatus status;

    /**
     * generated column (STORED)
     * insert/update 불가
     */
    @Column(
            name = "active_flag",
            insertable = false,
            updatable = false,
            columnDefinition = "TINYINT GENERATED ALWAYS AS (CASE WHEN status IN ('REQUESTED','IN_PROGRESS') THEN 1 ELSE NULL END) STORED"
    )
    private Integer activeFlag;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public static BattleRoom create(Long requesterId, Long opponentId) {

        if (requesterId.equals(opponentId)) {
            throw new BusinessException(ErrorCode.SELF_BATTLE_REQUEST_NOT_ALLOWED);
        }

        Long low = Math.min(requesterId, opponentId);
        Long high = Math.max(requesterId, opponentId);

        return BattleRoom.builder()
                .latestRoundNumber(0L)
                .requester(requesterId)
                .opponent(opponentId)
                .userLowId(low)
                .userHighId(high)
                .status(BattleRoomStatus.REQUESTED)
                .build();
    }

    public void startBattle() {
        if (status != BattleRoomStatus.REQUESTED) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_CLOSED);
        }
        status = BattleRoomStatus.IN_PROGRESS;
    }

    public Long increaseRoundNumber() {
        return latestRoundNumber = latestRoundNumber + 1;
    }

    public void close() {
        if (status == BattleRoomStatus.CLOSED) {
            throw new BusinessException(ErrorCode.BATTLE_ROOM_CLOSED);
        }
        status = BattleRoomStatus.CLOSED;
    }
}