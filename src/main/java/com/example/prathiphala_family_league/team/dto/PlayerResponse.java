package com.example.prathiphala_family_league.team.dto;

import com.example.prathiphala_family_league.team.entity.Player;
import com.example.prathiphala_family_league.team.entity.PlayerRole;
import lombok.Getter;

@Getter
public class PlayerResponse {

    private final Long id;
    private final Long teamId;
    private final String teamName;
    private final String playerName;
    private final PlayerRole playerRole;
    private final Integer jerseyNumber;
    private final String country;

    public PlayerResponse(Player player) {
        this.id = player.getId();
        this.teamId = player.getTeam().getId();
        this.teamName = player.getTeam().getTeamName();
        this.playerName = player.getPlayerName();
        this.playerRole = player.getPlayerRole();
        this.jerseyNumber = player.getJerseyNumber();
        this.country = player.getCountry();
    }
}
