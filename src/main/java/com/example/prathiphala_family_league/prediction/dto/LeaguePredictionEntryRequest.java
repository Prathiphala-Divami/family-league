package com.example.prathiphala_family_league.prediction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LeaguePredictionEntryRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Rank is required")
    @Min(value = 1, message = "Rank must be at least 1")
    private Integer rank;
}
