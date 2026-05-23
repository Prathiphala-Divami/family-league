package com.example.prathiphala_family_league.common.exception;

public class InvalidResultException extends FamilyLeagueException {

    public InvalidResultException(String message) {
        super("INVALID_RESULT", message);
    }
}
