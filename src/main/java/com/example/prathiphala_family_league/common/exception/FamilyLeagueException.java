package com.example.prathiphala_family_league.common.exception;

import lombok.Getter;

@Getter
public abstract class FamilyLeagueException extends RuntimeException {

    private final String errorCode;

    protected FamilyLeagueException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
