package com.rsp.battle.battle.history.application;

import com.rsp.battle.battle.domain.Move;
import com.rsp.battle.battle.history.presentation.BattleResult;
import com.rsp.battle.battle.history.presentation.BattleRoundHistoryResponse;
import com.rsp.battle.battle.history.presentation.BattleRoundStatResponse;
import com.rsp.battle.battle.persistence.BattleRoundRepository;
import com.rsp.battle.battle.persistence.BattleStatProjection;
import com.rsp.battle.user.presentation.dto.response.Paginated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BattleRoundQueryService {

    private final BattleRoundRepository battleRoundRepository;

    public BattleRoundStatResponse getBattleStat(Long userId) {
        BattleStatProjection battleStatProjection = battleRoundRepository.findBattleStatByUserId(userId);

        double winRate =
                battleStatProjection.getTotalCount() == 0
                        ? 0
                        : (double) battleStatProjection.getWinCount() / battleStatProjection.getTotalCount();

        return BattleRoundStatResponse.of(
                userId,
                battleStatProjection.getTotalCount(),
                battleStatProjection.getWinCount(),
                battleStatProjection.getLoseCount(),
                battleStatProjection.getDrawCount(),
                winRate
        );
    }

    public Paginated<BattleRoundHistoryResponse> getBattleHistory(
            Long userId,
            String keyword,
            BattleResult battleResult,
            Pageable pageable
    ) {
        Page<BattleRoundHistoryResponse> battleRoundHistoryResponsePage = battleRoundRepository.searchBattleResult(
                userId,
                keyword,
                battleResult != null ? battleResult.name() : null,
                pageable
        ).map(projection -> {
                    Move myMove = Objects.equals(userId, projection.getRequesterId())
                            ? projection.getRequesterMove()
                            : projection.getOpponentMove();
                    Move opponentMove = Objects.equals(userId, projection.getRequesterId())
                            ? projection.getOpponentMove()
                            : projection.getRequesterMove();

                    BattleResult result = projection.getWinnerUserId() == null
                            ? BattleResult.DRAW
                            : Objects.equals(userId, projection.getWinnerUserId())
                                    ? BattleResult.WIN
                                    : BattleResult.LOSE;

                    return new BattleRoundHistoryResponse(
                            projection.getId(),
                            projection.getNickName(),
                            projection.getProfileImageKey(),
                            myMove,
                            opponentMove,
                            result,
                            projection.getCreatedAt()
                    );
                });

        return Paginated.from(battleRoundHistoryResponsePage);
    }
}
