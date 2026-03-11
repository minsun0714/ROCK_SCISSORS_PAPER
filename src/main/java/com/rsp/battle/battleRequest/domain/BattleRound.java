package com.rsp.battle.battleRequest.domain;

import com.rsp.battle.common.exception.BusinessException;
import com.rsp.battle.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(
        name = "battle_round",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_round_room_number",
                        columnNames = {"room_id", "round_number"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class BattleRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * battle_room FK
     */
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "round_number", nullable = false)
    private Long roundNumber;

    @Column(name = "requester_id")
    private Long requesterId;

    @Column(name = "opponent_id")
    private Long opponentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "requester_move")
    private Move requesterMove;

    @Enumerated(EnumType.STRING)
    @Column(name = "opponent_move")
    private Move opponentMove;

    @Column(name = "winner_user_id")
    private Long winnerUserId = null;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * 라운드 생성
     */
    public static BattleRound create(Long roomId, Long roundNumber, Long requesterId, Long opponentId) {
        return BattleRound.builder()
                .requesterId(requesterId)
                .opponentId(opponentId)
                .roomId(roomId)
                .roundNumber(roundNumber)
                .build();
    }

    /**
     * requester move 입력
     */
    public void submitRequesterMove(Move move) {
        this.requesterMove = move;
    }

    /**
     * opponent move 입력
     */
    public void submitOpponentMove(Move move) {
        this.opponentMove = move;
    }

    /**
     * round 종료 여부
     */
    public boolean isComplete() {
        return requesterMove != null && opponentMove != null;
    }

    /**
     * 승자 결정
     */
    public void decideWinner() {
        if (requesterMove == null) {
            this.winnerUserId = opponentId;
            return;
        }

        if (opponentMove == null) {
            this.winnerUserId = requesterId;
            return;
        }

        int result = requesterMove.fight(opponentMove);
        switch (result) {
            case 0 -> this.winnerUserId = null;
            case 1 -> this.winnerUserId = requesterId;
            case 2 -> this.winnerUserId = opponentId;
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}