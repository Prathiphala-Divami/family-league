package com.example.prathiphala_family_league.league.entity;

import com.example.prathiphala_family_league.team.entity.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "season_team")
public class SeasonTeam {

    @EmbeddedId
    private SeasonTeamId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("seasonId")
    @JoinColumn(name = "season_id")
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    public SeasonTeam(Season season, Team team) {
        this.id = new SeasonTeamId(season.getId(), team.getId());
        this.season = season;
        this.team = team;
    }
}
