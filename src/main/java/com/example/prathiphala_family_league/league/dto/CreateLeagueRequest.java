package com.example.prathiphala_family_league.league.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CreateLeagueRequest {

    @NotBlank(message = "League name is required")
    @Size(max = 100, message = "League name must not exceed 100 characters")
    private String name;

    private String description;
}
