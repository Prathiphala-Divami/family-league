package com.example.prathiphala_family_league.match.dto;

import com.example.prathiphala_family_league.match.entity.ResultType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PublishResultRequest {

    @NotNull(message = "Result type is required")
    private ResultType resultType;

    // Required for WIN; must be null for TIE / NO_RESULT — validated at service layer.
    private Long winningTeamId;

    private Long tossWinnerTeamId;

    // Required for WIN; must be null for TIE / NO_RESULT — validated at service layer.
    private Long playerOfMatchId;

    @Size(max = 100, message = "Winning margin must not exceed 100 characters")
    private String winningMargin;
}
