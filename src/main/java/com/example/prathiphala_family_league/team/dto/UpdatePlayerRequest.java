package com.example.prathiphala_family_league.team.dto;

import com.example.prathiphala_family_league.team.entity.PlayerRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdatePlayerRequest {

    @Size(max = 100, message = "Player name must not exceed 100 characters")
    private String playerName;

    private PlayerRole playerRole;

    @Min(value = 0, message = "Jersey number must be 0 or greater")
    @Max(value = 999, message = "Jersey number must not exceed 999")
    private Integer jerseyNumber;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;
}
