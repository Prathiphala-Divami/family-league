package com.example.prathiphala_family_league.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 100, message = "Team name must not exceed 100 characters")
    private String teamName;

    @Size(max = 10, message = "Short name must not exceed 10 characters")
    private String shortName;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logo;
}
