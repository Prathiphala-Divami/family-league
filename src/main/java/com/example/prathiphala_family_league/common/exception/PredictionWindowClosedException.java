package com.example.prathiphala_family_league.common.exception;

public class PredictionWindowClosedException extends FamilyLeagueException {

    public PredictionWindowClosedException() {
        super("PREDICTION_WINDOW_CLOSED", "The prediction window for this match is closed");
    }

    public PredictionWindowClosedException(String message) {
        super("PREDICTION_WINDOW_CLOSED", message);
    }
}
