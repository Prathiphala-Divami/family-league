package com.example.prathiphala_family_league.common.exception;

public class ResourceNotFoundException extends FamilyLeagueException {

    public ResourceNotFoundException(String resource, Long id) {
        super("RESOURCE_NOT_FOUND", resource + " not found with id: " + id);
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
