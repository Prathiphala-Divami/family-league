package com.example.prathiphala_family_league.common.exception;

public class UnauthorisedActionException extends FamilyLeagueException {

    public UnauthorisedActionException(String message) {
        super("UNAUTHORISED_ACTION", message);
    }
}
