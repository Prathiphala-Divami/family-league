package com.example.prathiphala_family_league.prediction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class SubmitLeaguePredictionRequest {

    @NotEmpty(message = "Predictions list must not be empty")
    @Valid
    private List<LeaguePredictionEntryRequest> predictions;
}
