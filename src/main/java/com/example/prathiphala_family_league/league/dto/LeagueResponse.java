package com.example.prathiphala_family_league.league.dto;

import com.example.prathiphala_family_league.league.entity.League;
import lombok.Getter;

import java.time.Instant;

@Getter
public class LeagueResponse {

    private final Long id;
    private final String name;
    private final String description;
    private final Instant createdAt;

    public LeagueResponse(League league) {
        this.id = league.getId();
        this.name = league.getName();
        this.description = league.getDescription();
        this.createdAt = league.getCreatedAt();
    }
}
