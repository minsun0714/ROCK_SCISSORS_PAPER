package com.rsp.battle.battle.persistence;

import com.rsp.battle.battle.domain.BattleRound;
import com.rsp.battle.battle.history.presentation.BattleResult;
import com.rsp.battle.battle.history.presentation.BattleRoundHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BattleRoundRepository extends JpaRepository<BattleRound, Long> {

    BattleRound findFirstByRoomIdOrderByRoundNumberDesc(Long roomId);

    @Query("""
            SELECT COUNT(r) AS totalCount,
                    SUM(CASE WHEN r.winnerUserId = :userId THEN 1 ELSE 0 END) AS winCount,
                    SUM(CASE WHEN r.winnerUserId IS NOT NULL AND r.winnerUserId != :userId THEN 1 ELSE 0 END) AS loseCount,
                    SUM(CASE WHEN r.winnerUserId IS NULL THEN 1 ELSE 0 END) AS drawCount
            FROM BattleRound r
            WHERE r.requesterId = :userId OR r.opponentId = :userId
    """)
    BattleStatProjection findBattleStatByUserId(@Param("userId") Long userId);

    @Query(
            value = """
            SELECT u.id, u.nickname, u.profile_image_key,
                r.requester_id, r.opponent_id, r.requester_move, r.opponent_move, r.winner_user_id, r.created_at
            FROM users u JOIN battle_round r
                ON u.id = CASE
                    WHEN r.requester_id = :userId THEN r.opponent_id
                    WHEN r.opponent_id = :userId THEN r.requester_id
                END
            WHERE
            (r.requester_id = :userId OR r.opponent_id = :userId)
            AND (LENGTH(:keyword) < 2 OR MATCH(u.nickname) AGAINST(:keyword IN BOOLEAN MODE))
            AND (
                :battleResult IS NULL
                OR (:battleResult = 'WIN' AND r.winner_user_id = :userId)
                OR (:battleResult = 'LOSE' AND r.winner_user_id IS NOT NULL AND r.winner_user_id != :userId)
                OR (:battleResult = 'DRAW' AND r.winner_user_id IS NULL)
            )
            ORDER BY r.created_at DESC
            """,

            countQuery = """
            SELECT COUNT(*)
            FROM users u JOIN battle_round r
                ON u.id = CASE
                    WHEN r.requester_id = :userId THEN r.opponent_id
                    WHEN r.opponent_id = :userId THEN r.requester_id
                END
            WHERE
            (r.requester_id = :userId OR r.opponent_id = :userId)
            AND (LENGTH(:keyword) < 2 OR MATCH(u.nickname) AGAINST(:keyword IN BOOLEAN MODE))
            AND (
                :battleResult IS NULL
                OR (:battleResult = 'WIN' AND r.winner_user_id = :userId)
                OR (:battleResult = 'LOSE' AND r.winner_user_id IS NOT NULL AND r.winner_user_id != :userId)
                OR (:battleResult = 'DRAW' AND r.winner_user_id IS NULL)
            )
            """,

            nativeQuery = true
    )

    Page<BattleRoundHistoryProjection> searchBattleResult(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("battleResult") String battleResult,
            Pageable pageable
    );
}
