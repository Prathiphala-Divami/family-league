package com.example.prathiphala_family_league.match.dto;

import com.example.prathiphala_family_league.match.entity.MatchResult;
import com.example.prathiphala_family_league.match.entity.ResultType;
import lombok.Getter;

import java.time.Instant;

@Getter
public class MatchResultResponse {

    private final Long id;
    private final Long matchId;
    private final ResultType resultType;
    private final Long winningTeamId;
    private final String winningTeamName;
    private final Long tossWinnerTeamId;
    private final String tossWinnerTeamName;
    private final Long playerOfMatchId;
    private final String playerOfMatchName;
    private final String winningMargin;
    private final Long publishedById;
    private final String publishedByName;
    private final Instant publishedAt;

    public MatchResultResponse(MatchResult r) {
        this.id = r.getId();
        this.matchId = r.getMatch().getId();
        this.resultType = r.getResultType();
        this.winningTeamId = r.getWinningTeam() != null ? r.getWinningTeam().getId() : null;
        this.winningTeamName = r.getWinningTeam() != null ? r.getWinningTeam().getTeamName() : null;
        this.tossWinnerTeamId = r.getTossWinnerTeam() != null ? r.getTossWinnerTeam().getId() : null;
        this.tossWinnerTeamName = r.getTossWinnerTeam() != null ? r.getTossWinnerTeam().getTeamName() : null;
        this.playerOfMatchId = r.getPlayerOfMatch() != null ? r.getPlayerOfMatch().getId() : null;
        this.playerOfMatchName = r.getPlayerOfMatch() != null ? r.getPlayerOfMatch().getPlayerName() : null;
        this.winningMargin = r.getWinningMargin();
        this.publishedById = r.getPublishedBy().getId();
        this.publishedByName = r.getPublishedBy().getName();
        this.publishedAt = r.getPublishedAt();
    }
}
