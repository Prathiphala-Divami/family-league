package com.example.prathiphala_family_league.common.exception;

public class DuplicateResultException extends FamilyLeagueException {
    public DuplicateResultException(String message) {
        super("DUPLICATE_RESULT", message);
    }
}
