package com.example.prathiphala_family_league.common.exception;

public class SeasonClosedException extends FamilyLeagueException {

    public SeasonClosedException() {
        super("SEASON_CLOSED", "This season is closed and no further changes are allowed");
    }

    public SeasonClosedException(String message) {
        super("SEASON_CLOSED", message);
    }
}
