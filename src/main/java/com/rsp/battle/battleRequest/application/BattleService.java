package com.rsp.battle.battleRequest.application;

import com.rsp.battle.battleRequest.domain.BattleRoom;
import com.rsp.battle.battleRequest.domain.BattleRound;
import com.rsp.battle.battleRequest.domain.Move;
import com.rsp.battle.battleRequest.persistence.BattleRoomRepository;
import com.rsp.battle.battleRequest.persistence.BattleRoundRepository;
import com.rsp.battle.battleRequest.presentation.dto.response.BattleResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class BattleService {

    private final BattleRoomRepository battleRoomRepository;
    private final BattleRoundRepository battleRoundRepository;

    @Transactional
    public void startBattleRound(Long roomId){
        BattleRoom room = battleRoomRepository.findByIdForUpdate(roomId);

        long roundNumber = room.increaseRoundNumber();

        BattleRound battleRound = BattleRound.create(
                roomId,
                roundNumber,
                room.getRequester(),
                room.getOpponent()
        );

        battleRoundRepository.save(battleRound);
    }

    @Transactional
    public boolean move(Long roomId, Long userId, Move move) {
        BattleRound battleRound = battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(roomId);

        if (Objects.equals(battleRound.getRequesterId(), userId)) {
            battleRound.submitRequesterMove(move);
        } else {
            battleRound.submitOpponentMove(move);
        }

        return battleRound.isComplete();
    }

    @Transactional
    public BattleResultResponse decideWinner(Long roomId) {
        BattleRound battleRound = battleRoundRepository.findFirstByRoomIdOrderByRoundNumberDesc(roomId);

        battleRound.decideWinner();

        return BattleResultResponse.from(battleRound);
    }

    @Transactional
    public void close(Long roomId) {
        BattleRoom room = battleRoomRepository.findByIdForUpdate(roomId);

        room.close();
    }
}
