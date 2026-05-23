package com.example.prathiphala_family_league.prediction.dto;

import lombok.Getter;

@Getter
public class SubmitPredictionRequest {

    // All fields are optional — a user may predict any subset of the three outcomes.
    private Long predictedWinnerTeamId;
    private Long predictedTossWinnerId;
    private Long predictedPlayerOfMatchId;
}
