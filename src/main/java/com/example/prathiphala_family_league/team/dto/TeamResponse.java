package com.example.prathiphala_family_league.team.dto;

import com.example.prathiphala_family_league.team.entity.Team;
import lombok.Getter;

@Getter
public class TeamResponse {

    private final Long id;
    private final String teamName;
    private final String shortName;
    private final String logo;

    public TeamResponse(Team team) {
        this.id = team.getId();
        this.teamName = team.getTeamName();
        this.shortName = team.getShortName();
        this.logo = team.getLogo();
    }
}
