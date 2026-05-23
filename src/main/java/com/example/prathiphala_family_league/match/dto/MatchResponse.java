package com.example.prathiphala_family_league.match.dto;

import com.example.prathiphala_family_league.match.entity.Match;
import com.example.prathiphala_family_league.match.entity.MatchStatus;
import com.example.prathiphala_family_league.team.dto.TeamResponse;
import lombok.Getter;

import java.time.Instant;

@Getter
public class MatchResponse {

    private final Long id;
    private final Long seasonId;
    private final String seasonName;
    private final TeamResponse team1;
    private final TeamResponse team2;
    private final String venue;
    private final Instant startTime;
    private final Instant predictionLockTime;
    private final Integer matchNumber;
    private final MatchStatus status;
    private final Instant createdAt;

    public MatchResponse(Match match) {
        this.id = match.getId();
        this.seasonId = match.getSeason().getId();
        this.seasonName = match.getSeason().getName();
        this.team1 = new TeamResponse(match.getTeam1());
        this.team2 = new TeamResponse(match.getTeam2());
        this.venue = match.getVenue();
        this.startTime = match.getStartTime();
        this.predictionLockTime = match.getPredictionLockTime();
        this.matchNumber = match.getMatchNumber();
        this.status = match.getStatus();
        this.createdAt = match.getCreatedAt();
    }
}
