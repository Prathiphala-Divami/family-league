package com.example.prathiphala_family_league.league.dto;

import com.example.prathiphala_family_league.league.entity.Season;
import com.example.prathiphala_family_league.league.entity.SeasonStatus;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
public class SeasonResponse {

    private final Long id;
    private final Long leagueId;
    private final String leagueName;
    private final String name;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final SeasonStatus status;
    private final Instant predictionLockTime;
    private final Instant createdAt;

    public SeasonResponse(Season season) {
        this.id = season.getId();
        this.leagueId = season.getLeague().getId();
        this.leagueName = season.getLeague().getName();
        this.name = season.getName();
        this.startDate = season.getStartDate();
        this.endDate = season.getEndDate();
        this.status = season.getStatus();
        this.predictionLockTime = season.getPredictionLockTime();
        this.createdAt = season.getCreatedAt();
    }
}
