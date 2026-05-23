package com.example.prathiphala_family_league.prediction.dto;

import com.example.prathiphala_family_league.prediction.entity.Prediction;
import lombok.Getter;

import java.time.Instant;

@Getter
public class PredictionResponse {

    private final Long id;
    private final Long matchId;
    private final Long userId;
    private final Long predictedWinnerTeamId;
    private final String predictedWinnerTeamName;
    private final Long predictedTossWinnerId;
    private final String predictedTossWinnerName;
    private final Long predictedPlayerOfMatchId;
    private final String predictedPlayerOfMatchName;
    private final int pointsEarned;
    private final Instant submittedAt;

    public PredictionResponse(Prediction p) {
        this.id = p.getId();
        this.matchId = p.getMatch().getId();
        this.userId = p.getUser().getId();
        this.predictedWinnerTeamId = p.getPredictedWinnerTeam() != null
                ? p.getPredictedWinnerTeam().getId() : null;
        this.predictedWinnerTeamName = p.getPredictedWinnerTeam() != null
                ? p.getPredictedWinnerTeam().getTeamName() : null;
        this.predictedTossWinnerId = p.getPredictedTossWinner() != null
                ? p.getPredictedTossWinner().getId() : null;
        this.predictedTossWinnerName = p.getPredictedTossWinner() != null
                ? p.getPredictedTossWinner().getTeamName() : null;
        this.predictedPlayerOfMatchId = p.getPredictedPlayerOfMatch() != null
                ? p.getPredictedPlayerOfMatch().getId() : null;
        this.predictedPlayerOfMatchName = p.getPredictedPlayerOfMatch() != null
                ? p.getPredictedPlayerOfMatch().getPlayerName() : null;
        this.pointsEarned = p.getPointsEarned();
        this.submittedAt = p.getSubmittedAt();
    }
}
