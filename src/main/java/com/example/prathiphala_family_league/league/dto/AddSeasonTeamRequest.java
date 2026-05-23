package com.example.prathiphala_family_league.league.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AddSeasonTeamRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;
}
