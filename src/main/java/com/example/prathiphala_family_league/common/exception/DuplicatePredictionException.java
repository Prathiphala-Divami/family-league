package com.example.prathiphala_family_league.common.exception;

public class DuplicatePredictionException extends FamilyLeagueException {

    public DuplicatePredictionException() {
        super("DUPLICATE_PREDICTION", "A prediction has already been submitted for this match");
    }

    public DuplicatePredictionException(String message) {
        super("DUPLICATE_PREDICTION", message);
    }
}
