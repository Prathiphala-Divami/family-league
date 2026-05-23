package com.example.prathiphala_family_league.league.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateSeasonRequest {

    @NotBlank(message = "Season name is required")
    @Size(max = 100, message = "Season name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
}
