package com.example.prathiphala_family_league.common.exception;

public class InvalidSeasonTeamException extends FamilyLeagueException {

    public InvalidSeasonTeamException(String message) {
        super("INVALID_SEASON_TEAM", message);
    }
}
