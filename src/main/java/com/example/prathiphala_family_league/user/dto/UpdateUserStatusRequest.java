package com.example.prathiphala_family_league.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateUserStatusRequest {

    @NotNull(message = "active flag is required")
    private Boolean active;
}
